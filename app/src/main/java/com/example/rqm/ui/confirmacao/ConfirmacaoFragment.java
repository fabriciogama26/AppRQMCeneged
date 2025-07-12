package com.example.rqm.ui.confirmacao;

import android.os.Bundle;
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
import com.example.rqm.utils.ExcelUtils;

import java.util.ArrayList;

public class ConfirmacaoFragment extends Fragment {

    // Componentes da interface
    private TextView tvRequisitorResumo, tvProjetoResumo, tvDataResumo,
            tvUsuarioResumo, tvTituloConfirmacao, tvObservacaoResumo;
    private RecyclerView recyclerResumo;
    private Button btnConfirmarResumo;

    // Dados recebidos da tela anterior
    private String requisitor = "";
    private String projeto = "";
    private String data = "";
    private String usuario = "";
    private String tipoOperacao = "";
    private ArrayList<Material> materiaisSelecionados = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_confirmacao, container, false);

        inicializarComponentes(view);     // Liga os componentes da tela
        recuperarDados();                 // Pega os dados vindos da tela anterior
        configurarRecycler();             // Exibe os materiais selecionados
        configurarBotoes(view);           // Define os comportamentos dos botões
        atualizarTituloToolbar();         // Muda o título da barra de navegação

        return view;
    }

    /**
     * Liga os elementos da interface com seus IDs do layout
     */
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

    /**
     * Recupera os dados passados por Bundle da tela anterior
     */
    private void recuperarDados() {
        Bundle args = getArguments();
        if (args != null) {
            requisitor = args.getString("requisitor", "");
            projeto = args.getString("projeto", "");
            data = args.getString("data", "");
            usuario = args.getString("usuario", "");
            tipoOperacao = args.getString("tipoOperacao", "");
            materiaisSelecionados = (ArrayList<Material>) args.getSerializable("materiaisSelecionados");
        }

        // Atualiza os textos na interface com os dados recebidos
        tvTituloConfirmacao.setText(tipoOperacao.equalsIgnoreCase("Devolução")
                ? "Confirmação de Devolução"
                : "Confirmação de Requisição");

        tvRequisitorResumo.setText("Requisitor: " + requisitor);
        tvProjetoResumo.setText("Projeto: " + projeto);
        tvDataResumo.setText("Data: " + data);
        tvUsuarioResumo.setText("Almoxarife: " + usuario);
        tvObservacaoResumo.setText("Observação: "); // Campo reservado
    }

    /**
     * Configura o RecyclerView com os materiais escolhidos
     */
    private void configurarRecycler() {
        recyclerResumo.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerResumo.setAdapter(new MaterialResumoAdapter(materiaisSelecionados));
    }

    /**
     * Define os comportamentos dos botões Confirmar e Voltar
     */
    private void configurarBotoes(View view) {


        // Botão Confirmar: Salva no banco e exporta Excel
        btnConfirmarResumo.setOnClickListener(v -> {
            // Cria objeto Requisicao com os dados atuais
            Requisicao req = new Requisicao();
            req.usuario = usuario;
            req.requisitor = requisitor;
            req.projeto = projeto;
            req.data = data;
            req.tipoOperacao = tipoOperacao;
            req.observacao = ""; // Pode ser ajustado com campo editável
            req.setMateriaisSelecionados(materiaisSelecionados); // Inclui todos os dados, inclusive LP e Serial

            // Salva no banco de dados local (SQLite)
            RequisicaoDAO dao = new RequisicaoDAO(requireContext());
            long resultado = dao.salvar(req);

            if (resultado > 0) {
                Toast.makeText(requireContext(), "Operação salva com sucesso!", Toast.LENGTH_SHORT).show();

                // Exporta e compartilha como Excel automaticamente
                ExcelUtils.compartilharRequisicao(requireContext(), req);

                // Volta para tela inicial
                Navigation.findNavController(view).navigate(R.id.nav_home);
            } else {
                Toast.makeText(requireContext(), "Erro ao salvar a operação!", Toast.LENGTH_LONG).show();
            }
        });

    }

    /**
     * Atualiza o título da Toolbar com base na operação
     */
    private void atualizarTituloToolbar() {
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
            if (appCompatActivity.getSupportActionBar() != null) {
                appCompatActivity.getSupportActionBar().setTitle(
                        tipoOperacao.equalsIgnoreCase("Devolução") ? "Devolução" : "Requisição"
                );
            }
        }
    }
}
