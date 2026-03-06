package com.example.rqm.ui.confirmacao;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rqm.R;
import com.example.rqm.adapters.MaterialResumoAdapter;
import com.example.rqm.data.RequisicaoDAO;
import com.example.rqm.models.Material;
import com.example.rqm.models.Requisicao;
import com.example.rqm.utils.AuthPrefs;
import com.example.rqm.utils.DateUtils;
import com.example.rqm.utils.ExcelUtils;
import com.example.rqm.utils.OperacaoTipo;
import com.example.rqm.utils.SupabasePrefs;
import com.example.rqm.utils.SupabaseUploader;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Executors;

public class ConfirmacaoFragment extends Fragment {

    private TextView tvRequisitorResumo;
    private TextView tvProjetoResumo;
    private TextView tvDataResumo;
    private TextView tvUsuarioResumo;
    private TextView tvTituloConfirmacao;
    private TextView tvObservacaoResumo;
    private RecyclerView recyclerResumo;
    private Button btnConfirmarResumo;

    private String requisitor = "";
    private String projeto = "";
    private String dataDisplay = "";
    private String dataIso = "";
    private String usuario = "";
    private String tipoOperacao = "";
    private ArrayList<Material> materiaisSelecionados = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_confirmacao, container, false);

        inicializarComponentes(view);
        recuperarDados();
        configurarRecycler();
        configurarBotoes(view);
        atualizarTituloToolbar();

        return view;
    }

    private void inicializarComponentes(View view) {
        tvTituloConfirmacao = view.findViewById(R.id.tvTituloConfirmacao);
        tvRequisitorResumo = view.findViewById(R.id.tvRequisitorResumo);
        tvProjetoResumo = view.findViewById(R.id.tvProjetoResumo);
        tvDataResumo = view.findViewById(R.id.tvDataResumo);
        tvUsuarioResumo = view.findViewById(R.id.tvUsuarioResumo);
        tvObservacaoResumo = view.findViewById(R.id.tvObservacaoResumo);
        recyclerResumo = view.findViewById(R.id.recyclerResumo);
        btnConfirmarResumo = view.findViewById(R.id.btnConfirmarResumo);
    }

    @SuppressWarnings("unchecked")
    private void recuperarDados() {
        Bundle args = getArguments();
        if (args != null) {
            requisitor = args.getString("requisitor", "");
            projeto = args.getString("projeto", "");
            dataDisplay = args.getString("data_display", "");
            if (dataDisplay.isEmpty()) {
                dataDisplay = args.getString("data", "");
            }
            dataIso = args.getString("data_iso", "");
            if (dataIso.isEmpty()) {
                dataIso = DateUtils.toIsoWithNow(dataDisplay);
            }
            usuario = args.getString("usuario", "");
            tipoOperacao = args.getString("tipoOperacao", "");
            materiaisSelecionados = (ArrayList<Material>) args.getSerializable("materiaisSelecionados");
        }

        tvTituloConfirmacao.setText(OperacaoTipo.isDevolucao(tipoOperacao)
                ? getString(R.string.confirmacao_titulo_devolucao)
                : getString(R.string.confirmacao_titulo_requisicao));

        tvRequisitorResumo.setText("Responsavel: " + requisitor);
        tvProjetoResumo.setText("Projeto: " + projeto);
        tvDataResumo.setText("Data: " + dataDisplay);
        tvUsuarioResumo.setText("Almoxarife: " + usuario);
    }

    private void configurarRecycler() {
        recyclerResumo.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerResumo.setAdapter(new MaterialResumoAdapter(materiaisSelecionados));
    }

    private void configurarBotoes(View view) {
        btnConfirmarResumo.setOnClickListener(v -> {
            Requisicao req = new Requisicao();
            req.usuario = usuario;
            req.requisitor = requisitor;
            req.projeto = projeto;
            req.data = dataIso;
            req.tipoOperacao = OperacaoTipo.normalize(tipoOperacao);
            req.observacao = tvObservacaoResumo.getText().toString().trim();
            req.origem = "APP";
            String imei = AuthPrefs.getImei(requireContext());
            if (imei == null || imei.isEmpty()) {
                imei = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            }
            req.deviceId = imei;
            if (req.clientRequestId == null || req.clientRequestId.trim().isEmpty()) {
                req.clientRequestId = UUID.randomUUID().toString();
            }
            req.setMateriaisSelecionados(materiaisSelecionados);

            RequisicaoDAO dao = new RequisicaoDAO(requireContext());
            long resultado = dao.salvar(req);

            if (resultado <= 0) {
                Toast.makeText(requireContext(), "Erro ao salvar a operacao.", Toast.LENGTH_LONG).show();
                return;
            }

            Toast.makeText(requireContext(), "Operacao salva com sucesso.", Toast.LENGTH_SHORT).show();

            int status = SupabasePrefs.getStatus(requireContext());
            boolean conectado = status == SupabasePrefs.STATUS_CONNECTED && SupabasePrefs.hasConfig(requireContext());
            boolean sessaoValidaServidor = !AuthPrefs.isTestSession(requireContext());

            if (conectado && sessaoValidaServidor) {
                btnConfirmarResumo.setEnabled(false);
                Executors.newSingleThreadExecutor().execute(() -> {
                    SupabaseUploader.UploadResult enviado = SupabaseUploader.enviarRequisicao(requireContext(), req);
                    requireActivity().runOnUiThread(() -> {
                        btnConfirmarResumo.setEnabled(true);
                        String msg = enviado.success ? "Base de dados atualizada." : enviado.message;
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                        ExcelUtils.compartilharRequisicao(requireContext(), req);
                        Navigation.findNavController(view).navigate(R.id.nav_home);
                    });
                });
                return;
            }

            if (status == SupabasePrefs.STATUS_CONNECTING) {
                Toast.makeText(requireContext(), "Conectando... exportacao local.", Toast.LENGTH_SHORT).show();
            } else if (!sessaoValidaServidor) {
                Toast.makeText(requireContext(), "Modo teste ativo. Exportacao local.", Toast.LENGTH_SHORT).show();
            }
            ExcelUtils.compartilharRequisicao(requireContext(), req);
            Navigation.findNavController(view).navigate(R.id.nav_home);
        });
    }

    private void atualizarTituloToolbar() {
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
            if (appCompatActivity.getSupportActionBar() != null) {
                appCompatActivity.getSupportActionBar().setTitle(getString(OperacaoTipo.toLabelResId(tipoOperacao)));
            }
        }
    }
}
