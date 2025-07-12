package com.example.rqm.ui.historico;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.ViewModelProvider;

import com.example.rqm.R;
import com.example.rqm.adapters.HistoricoAdapter;
import com.example.rqm.data.RequisicaoDAO;
import com.example.rqm.models.Requisicao;
import com.example.rqm.utils.ExportadorJson;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HistoricoFragment extends Fragment {

    private RecyclerView recyclerHistorico;
    private Button btnExportar, btnFiltrar, btnLimpar;
    private EditText dataFiltro, filtroProjeto;
    private Spinner filtroPrefixo, filtroTipoOperacao;
    private boolean isFormattingProjeto = false; // Flag para evitar loop no TextWatcher

    private HistoricoViewModel historicoViewModel; // ViewModel que trata lógica de negócios
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1001; // Código de permissão para Android < 11

    private Disposable disposable;
    private RequisicaoDAO dao;
    private List<Requisicao> listaFiltrada; // Lista usada para exportar o histórico

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_historico, container, false);

        // Inicializa ViewModel
        historicoViewModel = new ViewModelProvider(this).get(HistoricoViewModel.class);

        // Mapeamento dos componentes da interface
        recyclerHistorico = view.findViewById(R.id.recyclerHistorico);
        btnExportar = view.findViewById(R.id.btnExportarFiltro);
        btnFiltrar = view.findViewById(R.id.btnFiltrar);
        btnLimpar = view.findViewById(R.id.btnLimpar);
        dataFiltro = view.findViewById(R.id.dataFiltro);
        filtroPrefixo = view.findViewById(R.id.filtroPrefixo);
        filtroTipoOperacao = view.findViewById(R.id.filtroTipoOperacao);
        filtroProjeto = view.findViewById(R.id.filtroProjeto);

        // Configura layout da RecyclerView
        recyclerHistorico.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Inicializa o DAO
        dao = new RequisicaoDAO(requireContext());

        // Prepara os spinners com valores definidos nos arrays
        configurarSpinners();

        // Aplica formatação automática no EditText do projeto com base no prefixo
        configurarFormatacaoProjeto();

        // Ação ao clicar no campo de data
        dataFiltro.setOnClickListener(v -> abrirDatePicker(dataFiltro));

        // Ação para aplicar filtro
        btnFiltrar.setOnClickListener(v -> aplicarFiltro());

        // Ação para exportar e compartilhar os dados atualmente exibidos (filtrados)
        btnExportar.setOnClickListener(v -> {
            if (listaFiltrada == null || listaFiltrada.isEmpty()) {
                Toast.makeText(requireContext(), "Nenhum dado para exportar", Toast.LENGTH_SHORT).show();
                return;
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                int permissao = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permissao != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(requireContext(), "Permissão de armazenamento necessária", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            boolean sucesso = ExportadorJson.exportarECompartilhar(requireContext(), listaFiltrada);
            if (!sucesso) {
                Toast.makeText(requireContext(), "Erro ao compartilhar JSON", Toast.LENGTH_SHORT).show();
            }
        });

        // Ação para limpar filtros
        btnLimpar.setOnClickListener(v -> limparFiltros());

        // Carrega a lista inicial sem filtros
        carregarDadosIniciais();

        return view;
    }

    // Adiciona TextWatcher no campo projeto para aplicar formatação automática com base no prefixo
    private void configurarFormatacaoProjeto() {
        filtroProjeto.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override public void afterTextChanged(android.text.Editable s) {
                if (isFormattingProjeto) return;
                isFormattingProjeto = true;

                String prefixoSelecionado = filtroPrefixo.getSelectedItem().toString();
                String textoDigitado = s.toString().replaceAll("[^a-zA-Z0-9]", "");

                String formatado = historicoViewModel.formatarNumeroProjetoHistorico(prefixoSelecionado, textoDigitado);

                if (!formatado.equals(s.toString())) {
                    filtroProjeto.setText(formatado);
                    filtroProjeto.setSelection(formatado.length());
                }

                isFormattingProjeto = false;
            }
        });
    }

    // Carrega todos os dados do banco inicialmente
    private void carregarDadosIniciais() {
        disposable = Single.fromCallable(() -> dao.listarTodas())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::atualizarLista, error -> {
                    Log.e("HistoricoFragment", "Erro ao carregar dados", error);
                    Toast.makeText(requireContext(), "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
                });
    }

    // Preenche os Spinners com os valores definidos no strings.xml
    private void configurarSpinners() {
        ArrayAdapter<CharSequence> adapterPrefixo = ArrayAdapter.createFromResource(requireContext(),
                R.array.prefixo_array_filtro, R.layout.spinner_item_white);
        adapterPrefixo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filtroPrefixo.setAdapter(adapterPrefixo);

        ArrayAdapter<CharSequence> adapterOperacao = ArrayAdapter.createFromResource(requireContext(),
                R.array.operacao_array_filtro, R.layout.spinner_item_white);
        adapterOperacao.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filtroTipoOperacao.setAdapter(adapterOperacao);
    }

    // Aplica o filtro com base nos campos preenchidos
    private void aplicarFiltro() {
        // Obtém os valores dos campos
        String projeto = filtroProjeto.getText().toString().trim();
        String prefixo = filtroPrefixo.getSelectedItem().toString();

        // Se um prefixo específico estiver selecionado, validamos o número do projeto
        if (!prefixo.equals("Todos") && !projeto.isEmpty()) {
            // Usa o ViewModel para validar o formato do projeto
            if (!historicoViewModel.validarCamposHistorico(prefixo, projeto)) {
                Toast.makeText(requireContext(), "Formato inválido em Projeto.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Recupera os demais filtros
        String tipoOperacao = filtroTipoOperacao.getSelectedItem().toString();
        String data = dataFiltro.getText().toString().trim();

        // Combina prefixo e número do projeto se necessário
        String projetoCompleto = (!prefixo.equals("Todos") && !projeto.isEmpty()) ? prefixo + projeto : projeto;

        // Cancela chamada anterior, se houver
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }

        // Executa a filtragem no banco
        String finalProjeto = projetoCompleto;
        disposable = Single.fromCallable(() -> dao.filtrarRequisicoes(data, finalProjeto, prefixo, tipoOperacao))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::processarResultadoFiltro, error -> {
                    Log.e("Filtro", "Erro ao aplicar filtro", error);
                    Toast.makeText(requireContext(), "Erro ao aplicar filtro", Toast.LENGTH_SHORT).show();
                });
    }


    // Processa o resultado do filtro
    private void processarResultadoFiltro(List<Requisicao> resultado) {
        if (resultado.isEmpty()) {
            Toast.makeText(requireContext(), "Nenhum resultado encontrado", Toast.LENGTH_SHORT).show();
        }
        listaFiltrada = resultado;
        atualizarLista(resultado);
    }

    // Limpa os filtros e recarrega os dados
    private void limparFiltros() {
        filtroProjeto.setText("");
        filtroPrefixo.setSelection(0);
        filtroTipoOperacao.setSelection(0);
        dataFiltro.setText("");
        carregarDadosIniciais();
    }

    // Atualiza a RecyclerView e salva lista filtrada
    private void atualizarLista(List<Requisicao> lista) {
        listaFiltrada = lista;
        if (getActivity() != null && isAdded()) {
            HistoricoAdapter adapter = new HistoricoAdapter(lista);
            recyclerHistorico.setAdapter(adapter);
        }
    }

    // Abre o DatePickerDialog para o usuário selecionar uma data
    private void abrirDatePicker(EditText campo) {
        Calendar calendario = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    // Formata a data no padrão brasileiro: dia/mês/ano
                    String dataFormatada = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    campo.setText(dataFormatada);
                },
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }


    // Libera os recursos do RxJava ao destruir o fragmento
    @Override
    public void onDestroy() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        super.onDestroy();
    }
}
