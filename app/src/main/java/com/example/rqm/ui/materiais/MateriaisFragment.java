package com.example.rqm.ui.materiais;

// Importações necessárias para funcionamento do fragmento e UI
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;

import com.example.rqm.R;
import com.example.rqm.adapters.MaterialAdapter;
import com.example.rqm.models.Material;
import com.example.rqm.ui.confirmacao.ConfirmacaoFragment;

import java.util.ArrayList;
import java.util.List;

public class MateriaisFragment extends Fragment {

    // ViewModel que armazena os dados da tela (materiais disponíveis e selecionados)
    private MateriaisViewModel viewModel;

    // Adapter que irá popular o RecyclerView com os materiais
    private MaterialAdapter adapter;

    private RecyclerView recyclerView;
    private String tipoOperacao = "";
    private AutoCompleteTextView autoCompleteBuscar;
    private Button btnFinalizar, btnCancelar;
    private List<String> listaOriginal = new ArrayList<>(); // Lista com os nomes dos materiais para autocomplete

    // Dados recebidos da tela anterior
    private String requisitor, usuario, projeto, data;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializa o ViewModel
        viewModel = new ViewModelProvider(this).get(MateriaisViewModel.class);

        // Se vieram argumentos da tela anterior, recupera e salva
        if (getArguments() != null) {
            tipoOperacao = getArguments().getString("tipoOperacao", "");
            requisitor = getArguments().getString("requisitor", "");
            usuario = getArguments().getString("usuario", "");
            projeto = getArguments().getString("projeto", "");
            data = getArguments().getString("data", "");
        }

        // Define o tipo de operação no ViewModel
        viewModel.setTipoOperacao(tipoOperacao);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_materiais, container, false);

        recyclerView = view.findViewById(R.id.recyclerMateriais);
        btnFinalizar = view.findViewById(R.id.btnFinalizar);
        btnCancelar = view.findViewById(R.id.btnCancelar);
        autoCompleteBuscar = view.findViewById(R.id.autoCompleteMaterial);
        TextView emptyText = view.findViewById(R.id.emptyText);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MaterialAdapter(
                new ArrayList<>(),
                material -> viewModel.removerMaterial(material),
                viewModel.getCodigosComLP() // <- Códigos especiais com layout LP
        );

        recyclerView.setAdapter(adapter);

        viewModel.getMateriaisSelecionados().observe(getViewLifecycleOwner(), materiais -> {
            adapter.setMaterialList(materiais);
            emptyText.setVisibility(materiais.isEmpty() ? View.VISIBLE : View.GONE);
        });

        listaOriginal.clear();
        for (Material m : viewModel.getListaCompleta()) {
            listaOriginal.add(m.codigo + " - " + m.descricao);
        }

        ArrayAdapter<String> sugestaoAdapter = new ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>(listaOriginal)
        ) {
            @NonNull
            @Override
            public android.widget.Filter getFilter() {
                return new android.widget.Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults results = new FilterResults();
                        List<String> filtered = new ArrayList<>();
                        if (constraint != null && constraint.length() > 0) {
                            String termo = constraint.toString().toLowerCase().trim();
                            for (String item : listaOriginal) {
                                if (item.toLowerCase().contains(termo)) {
                                    filtered.add(item);
                                }
                            }
                        } else {
                            filtered.addAll(listaOriginal);
                        }
                        results.values = filtered;
                        results.count = filtered.size();
                        return results;
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        clear();
                        if (results.values != null) {
                            addAll((List<String>) results.values);
                        }
                        notifyDataSetChanged();
                    }

                    @Override
                    public CharSequence convertResultToString(Object resultValue) {
                        return resultValue.toString();
                    }
                };
            }
        };

        autoCompleteBuscar.setAdapter(sugestaoAdapter);
        autoCompleteBuscar.setThreshold(1);

        autoCompleteBuscar.setOnItemClickListener((parent, view1, position, id) -> {
            String itemSelecionado = (String) parent.getItemAtPosition(position);
            if (itemSelecionado != null && itemSelecionado.contains(" - ")) {
                String codigo = itemSelecionado.split(" - ")[0].trim();
                viewModel.adicionarMaterial(codigo);
                autoCompleteBuscar.setText("");
            }
        });

        btnFinalizar.setOnClickListener(v -> {
            boolean algumPreenchido = false;
            boolean algumInvalido = false;

            List<Material> materiaisSelecionados = viewModel.getMateriaisSelecionados().getValue();

            for (int i = 0; i < recyclerView.getChildCount(); i++) {
                View itemView = recyclerView.getChildAt(i);

                EditText edtQtd = itemView.findViewById(R.id.edtQuantidade);
                EditText edtLP = itemView.findViewById(R.id.edtLP);
                EditText edtSerial = itemView.findViewById(R.id.edtSerial);

                String qtdTexto = edtQtd != null ? edtQtd.getText().toString().trim() : "";
                String lpTexto = edtLP != null ? edtLP.getText().toString().trim() : "";
                String serialTexto = edtSerial != null ? edtSerial.getText().toString().trim() : "";

                // Verifica a quantidade
                if (qtdTexto.isEmpty() || qtdTexto.equals("0")) {
                    algumInvalido = true;
                } else {
                    algumPreenchido = true;
                }

                // Atualiza os campos no objeto Material correspondente
                if (materiaisSelecionados != null && i < materiaisSelecionados.size()) {
                    Material m = materiaisSelecionados.get(i);

                    // Quantidade
                    try {
                        m.quantidade = Integer.parseInt(qtdTexto);
                    } catch (NumberFormatException e) {
                        m.quantidade = 0;
                    }

                    // LP e Serial (apenas se existirem no layout)
                    if (edtLP != null) m.lp = lpTexto;
                    if (edtSerial != null) m.serial = serialTexto;

                    // Se for material especial, exige LP e Serial preenchidos
                    if (viewModel.getCodigosComLP().contains(m.codigo)) {
                        if (lpTexto.isEmpty() || serialTexto.isEmpty()) {
                            algumInvalido = true;
                        }
                    }
                }
            }

            if (!algumPreenchido) {
                Toast.makeText(getContext(), "Preencha valor válido.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (algumInvalido) {
                Toast.makeText(getContext(), "Corrija os campos vazios.", Toast.LENGTH_SHORT).show();
                return;
            }

            String mensagem = viewModel.getMensagemConfirmacao();

            new AlertDialog.Builder(requireContext())
                    .setTitle("Confirmação")
                    .setMessage(mensagem)
                    .setPositiveButton("Sim", (dialog, which) -> {
                        // Cria bundle com todos os dados recebidos da tela anterior + materiais
                        Bundle bundle = new Bundle();
                        bundle.putString("tipoOperacao", tipoOperacao);
                        bundle.putString("requisitor", requisitor);
                        bundle.putString("usuario", usuario);
                        bundle.putString("projeto", projeto);
                        bundle.putString("data", data);

                        // Lista de materiais atualizada com LP e Serial
                        ArrayList<Material> listaSelecionada = new ArrayList<>(materiaisSelecionados);
                        bundle.putSerializable("materiaisSelecionados", listaSelecionada);

                        // Navega para a próxima tela
                        Navigation.findNavController(v).navigate(R.id.nav_confirmacao, bundle);
                    })
                    .setNegativeButton("Não", null)
                    .show();
        });


        btnCancelar.setOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }
}
