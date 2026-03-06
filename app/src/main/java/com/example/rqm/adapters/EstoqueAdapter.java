package com.example.rqm.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rqm.R;
import com.example.rqm.models.EstoqueItem;
import com.example.rqm.utils.DateUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class EstoqueAdapter extends RecyclerView.Adapter<EstoqueAdapter.ViewHolder> {

    private final List<EstoqueItem> itens = new ArrayList<>();
    private final DecimalFormat decimalFormat = new DecimalFormat("0.##");

    public void submitList(List<EstoqueItem> novosItens) {
        itens.clear();
        if (novosItens != null) {
            itens.addAll(novosItens);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_estoque, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EstoqueItem item = itens.get(position);
        holder.tvCodigo.setText("Codigo: " + safe(item.codigo));
        holder.tvDescricao.setText("Descricao: " + safe(item.descricao));
        holder.tvQuantidade.setText("Quantidade: " + decimalFormat.format(item.quantidade));

        if (item.projectScope) {
            holder.tvProjeto.setVisibility(View.VISIBLE);
            holder.tvProjeto.setText("Projeto: " + safe(item.projeto));
            holder.tvDetalhe.setVisibility(View.VISIBLE);
            holder.tvDetalhe.setText("Saido: " + decimalFormat.format(item.qtyIssued)
                    + " | Devolvido: " + decimalFormat.format(item.qtyReturned));
        } else {
            holder.tvProjeto.setVisibility(View.GONE);
            holder.tvDetalhe.setVisibility(View.GONE);
        }

        if (item.updatedAt != null && !item.updatedAt.isEmpty()) {
            holder.tvAtualizado.setVisibility(View.VISIBLE);
            holder.tvAtualizado.setText("Atualizado: " + DateUtils.toDisplayDate(item.updatedAt));
        } else {
            holder.tvAtualizado.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvCodigo;
        final TextView tvDescricao;
        final TextView tvQuantidade;
        final TextView tvProjeto;
        final TextView tvDetalhe;
        final TextView tvAtualizado;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCodigo = itemView.findViewById(R.id.tvEstoqueCodigo);
            tvDescricao = itemView.findViewById(R.id.tvEstoqueDescricao);
            tvQuantidade = itemView.findViewById(R.id.tvEstoqueQuantidade);
            tvProjeto = itemView.findViewById(R.id.tvEstoqueProjeto);
            tvDetalhe = itemView.findViewById(R.id.tvEstoqueDetalhe);
            tvAtualizado = itemView.findViewById(R.id.tvEstoqueAtualizado);
        }
    }

    private String safe(String value) {
        return value != null ? value : "";
    }
}
