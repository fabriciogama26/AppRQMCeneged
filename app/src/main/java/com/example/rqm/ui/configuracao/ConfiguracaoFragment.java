package com.example.rqm.ui.configuracao;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.rqm.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ConfiguracaoFragment extends Fragment {

    private ActivityResultLauncher<Intent> importarBancoLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_configuracao, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!com.example.rqm.utils.AuthPrefs.isAdmin(requireContext())) {
            Toast.makeText(requireContext(), "Acesso restrito a administradores.", Toast.LENGTH_SHORT).show();
            androidx.navigation.Navigation.findNavController(view).navigate(R.id.nav_home);
            return;
        }

        Button btnExportarBanco = view.findViewById(R.id.btnExportarBanco);
        Button btnImportarBanco = view.findViewById(R.id.btnImportarBanco);
        Button btnConfigurarSupabase = view.findViewById(R.id.btnConfigurarSupabase);

        btnExportarBanco.setOnClickListener(v -> exportarBancoDeDados());
        btnImportarBanco.setOnClickListener(v -> selecionarArquivoBanco());
        btnConfigurarSupabase.setOnClickListener(v ->
                androidx.navigation.Navigation.findNavController(view).navigate(R.id.nav_supabase_config));

        importarBancoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        importarBanco(uri);
                    }
                }
        );
    }

    private void exportarBancoDeDados() {
        try {
            String nomeDoBanco = "rqm.db";
            File bancoOrigem = requireContext().getDatabasePath(nomeDoBanco);
            File destino = new File(requireContext().getExternalFilesDir(null),
                    "backup_rqm_" + System.currentTimeMillis() + ".db");

            try (InputStream in = new FileInputStream(bancoOrigem);
                 OutputStream out = new FileOutputStream(destino)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }

            Toast.makeText(requireContext(), "Backup criado em: " + destino.getAbsolutePath(), Toast.LENGTH_LONG).show();

            Uri uri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".provider",
                    destino
            );

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Compartilhar banco de dados"));

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("ExportarBanco", "Erro ao exportar", e);
        }
    }

    private void selecionarArquivoBanco() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        importarBancoLauncher.launch(intent);
    }

    private void importarBanco(Uri uri) {
        try {
            String nomeArquivo = getFileName(uri);

            if (nomeArquivo == null || !nomeArquivo.endsWith(".db")) {
                Toast.makeText(requireContext(), "Arquivo inválido. Selecione o banco 'rqm.db'.", Toast.LENGTH_LONG).show();
                return;
            }

            File bancoAtual = requireContext().getDatabasePath("rqm.db");
            File destinoBackup = new File(requireContext().getExternalFilesDir(null),
                    "backup_rqm_antes_importacao_" + System.currentTimeMillis() + ".db");

            try (InputStream backupIn = new FileInputStream(bancoAtual);
                 OutputStream backupOut = new FileOutputStream(destinoBackup)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = backupIn.read(buffer)) > 0) {
                    backupOut.write(buffer, 0, length);
                }
            }

            try (InputStream in = requireContext().getContentResolver().openInputStream(uri);
                 OutputStream out = new FileOutputStream(bancoAtual, false)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }

            Toast.makeText(requireContext(), "Banco importado com sucesso!\nBackup criado em:\n" + destinoBackup.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Erro ao importar banco de dados.", Toast.LENGTH_SHORT).show();
            Log.e("ImportarBanco", "Erro: ", e);
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(android.provider.OpenableColumns.DISPLAY_NAME));
                }
            }
        }

        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }
}
