package com.example.rqm.ui.estoque;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rqm.R;
import com.example.rqm.adapters.EstoqueAdapter;
import com.example.rqm.utils.SupabaseEdgeClient;

import java.util.Collections;
import java.util.concurrent.Executors;

public class EstoqueFragment extends Fragment {

    private EstoqueAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyText;
    private EditText edtProjeto;
    private EditText edtCodigo;
    private EditText edtDescricao;
    private EditText edtQtdExata;
    private EditText edtQtdMin;
    private EditText edtQtdMax;
    private View progressView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_estoque, container, false);

        recyclerView = view.findViewById(R.id.recyclerEstoque);
        emptyText = view.findViewById(R.id.tvEstoqueEmpty);
        edtProjeto = view.findViewById(R.id.edtFiltroProjetoEstoque);
        edtCodigo = view.findViewById(R.id.edtFiltroCodigoEstoque);
        edtDescricao = view.findViewById(R.id.edtFiltroDescricaoEstoque);
        edtQtdExata = view.findViewById(R.id.edtFiltroQuantidadeExataEstoque);
        edtQtdMin = view.findViewById(R.id.edtFiltroQuantidadeMinEstoque);
        edtQtdMax = view.findViewById(R.id.edtFiltroQuantidadeMaxEstoque);
        progressView = view.findViewById(R.id.progressEstoque);

        Button btnFiltrar = view.findViewById(R.id.btnFiltrarEstoque);
        Button btnLimpar = view.findViewById(R.id.btnLimparEstoque);
        View filterHeader = view.findViewById(R.id.filterHeaderEstoque);
        View filterContent = view.findViewById(R.id.filterContentEstoque);
        ImageView filterArrow = view.findViewById(R.id.ivFilterArrowEstoque);

        adapter = new EstoqueAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        filterContent.setVisibility(View.GONE);
        filterArrow.setRotation(0f);
        filterHeader.setOnClickListener(v -> {
            boolean aberto = filterContent.getVisibility() == View.VISIBLE;
            filterContent.setVisibility(aberto ? View.GONE : View.VISIBLE);
            filterArrow.animate().rotation(aberto ? 0f : 180f).setDuration(180).start();
        });

        btnFiltrar.setOnClickListener(v -> carregarEstoque());
        btnLimpar.setOnClickListener(v -> {
            edtProjeto.setText("");
            edtCodigo.setText("");
            edtDescricao.setText("");
            edtQtdExata.setText("");
            edtQtdMin.setText("");
            edtQtdMax.setText("");
            carregarEstoque();
        });

        carregarEstoque();
        return view;
    }

    private void carregarEstoque() {
        progressView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);

        String projeto = texto(edtProjeto).toUpperCase();
        String codigo = texto(edtCodigo);
        String descricao = texto(edtDescricao);
        String qtyExact = texto(edtQtdExata);
        String qtyMin = texto(edtQtdMin);
        String qtyMax = texto(edtQtdMax);

        Executors.newSingleThreadExecutor().execute(() -> {
            SupabaseEdgeClient.EstoqueResult result = SupabaseEdgeClient.getEstoque(
                    requireContext(), projeto, codigo, descricao, qtyExact, qtyMin, qtyMax, 100, 0
            );

            if (!isAdded()) {
                return;
            }

            requireActivity().runOnUiThread(() -> {
                progressView.setVisibility(View.GONE);
                if (!result.success) {
                    adapter.submitList(Collections.emptyList());
                    recyclerView.setVisibility(View.GONE);
                    emptyText.setVisibility(View.VISIBLE);
                    emptyText.setText(TextUtils.isEmpty(result.message)
                            ? "Nao foi possivel carregar o estoque."
                            : result.message);
                    return;
                }

                adapter.submitList(result.items);
                boolean vazio = result.items == null || result.items.isEmpty();
                recyclerView.setVisibility(vazio ? View.GONE : View.VISIBLE);
                emptyText.setVisibility(vazio ? View.VISIBLE : View.GONE);
                emptyText.setText(vazio ? "Nenhum material encontrado." : "");

                if (!TextUtils.isEmpty(result.message) && !vazio) {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private String texto(EditText editText) {
        return editText != null ? editText.getText().toString().trim() : "";
    }
}
