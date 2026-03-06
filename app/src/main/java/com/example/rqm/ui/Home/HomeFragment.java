package com.example.rqm.ui.Home;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rqm.R;
import com.example.rqm.adapters.HomeHistoricoAdapter;
import com.example.rqm.data.RequisicaoDAO;
import com.example.rqm.models.Requisicao;
import com.example.rqm.models.SyncRun;
import com.example.rqm.utils.AuthPrefs;
import com.example.rqm.utils.OperacaoTipo;
import com.example.rqm.utils.SyncManager;
import com.example.rqm.utils.SyncRateLimiter;

import java.util.List;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private RecyclerView recycler;
    private TextView empty;
    private boolean autoSyncRequested;
    private boolean autoSyncStarted;
    private boolean syncEmAndamento;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        Button btnRequisicao = root.findViewById(R.id.btnRequisicao);
        Button btnDevolucao = root.findViewById(R.id.btnDevolucao);
        Button btnEstoque = root.findViewById(R.id.btnEstoque);
        Button btnSincronizarAgora = root.findViewById(R.id.btnSincronizarAgora);
        View operacoesHeader = root.findViewById(R.id.homeOperacoesHeader);
        View operacoesContent = root.findViewById(R.id.homeOperacoesContent);
        ImageView operacoesArrow = root.findViewById(R.id.ivOperacoesArrow);

        recycler = root.findViewById(R.id.recyclerHomeHistorico);
        empty = root.findViewById(R.id.tvHomeHistoricoEmpty);

        if (getArguments() != null) {
            autoSyncRequested = getArguments().getBoolean("auto_sync", false);
        }

        carregarHistorico();

        if (operacoesHeader != null && operacoesContent != null && operacoesArrow != null) {
            operacoesContent.setVisibility(View.VISIBLE);
            operacoesArrow.setRotation(180f);
            operacoesHeader.setOnClickListener(v -> {
                boolean aberto = operacoesContent.getVisibility() == View.VISIBLE;
                operacoesContent.setVisibility(aberto ? View.GONE : View.VISIBLE);
                operacoesArrow.animate().rotation(aberto ? 0f : 180f).setDuration(180).start();
            });
        }

        btnRequisicao.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putString("tipo_operacao", OperacaoTipo.REQ);
            Navigation.findNavController(view).navigate(R.id.nav_operacao, bundle);
        });

        btnDevolucao.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putString("tipo_operacao", OperacaoTipo.DEV);
            Navigation.findNavController(view).navigate(R.id.nav_operacao, bundle);
        });

        btnEstoque.setOnClickListener(view -> Navigation.findNavController(view).navigate(R.id.nav_estoque));
        btnSincronizarAgora.setOnClickListener(v -> iniciarSincronizacaoManual());

        root.post(() -> {
            if (autoSyncRequested && !autoSyncStarted && !AuthPrefs.isTestSession(requireContext())) {
                autoSyncStarted = true;
                autoSyncRequested = false;
                executarSincronizacao(true);
            }
        });

        return root;
    }

    private void iniciarSincronizacaoManual() {
        if (syncEmAndamento) {
            Toast.makeText(requireContext(), "Sincronizacao ja esta em andamento.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (AuthPrefs.isTestSession(requireContext())) {
            Toast.makeText(requireContext(), "Modo teste ativo. Sincronizacao com servidor desabilitada.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!SyncRateLimiter.canStartManualSync(requireContext())) {
            int segundos = SyncRateLimiter.getRemainingSeconds(requireContext());
            Toast.makeText(requireContext(), "Aguarde " + segundos + "s para sincronizar novamente.", Toast.LENGTH_SHORT).show();
            return;
        }

        SyncRateLimiter.markManualSyncStarted(requireContext());
        executarSincronizacao(false);
    }

    private void carregarHistorico() {
        if (recycler != null) {
            recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
            RequisicaoDAO dao = new RequisicaoDAO(requireContext());
            List<Requisicao> ultimas = dao.listarUltimas(5);
            recycler.setAdapter(new HomeHistoricoAdapter(ultimas));
            if (empty != null) {
                boolean semDados = ultimas == null || ultimas.isEmpty();
                empty.setVisibility(semDados ? View.VISIBLE : View.GONE);
                recycler.setVisibility(semDados ? View.GONE : View.VISIBLE);
            }
        }
    }

    private void executarSincronizacao(boolean automatico) {
        syncEmAndamento = true;
        AlertDialog dialog = criarDialogSincronizacao();
        dialog.show();

        Executors.newSingleThreadExecutor().execute(() -> {
            SyncRun run = SyncManager.executarSincronizacao(requireContext());
            if (!isAdded()) {
                syncEmAndamento = false;
                return;
            }
            requireActivity().runOnUiThread(() -> {
                syncEmAndamento = false;
                dialog.dismiss();
                carregarHistorico();
                String prefixo = automatico
                        ? "Sincronizacao automatica concluida. "
                        : "Sincronizacao concluida. ";
                Toast.makeText(requireContext(), prefixo + run.message, Toast.LENGTH_LONG).show();
            });
        });
    }

    private AlertDialog criarDialogSincronizacao() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_sync_progress, null, false);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
}
