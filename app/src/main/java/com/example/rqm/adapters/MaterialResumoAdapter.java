package com.example.rqm.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rqm.R;
import com.example.rqm.models.Material;

import java.util.List;

/**
 * Adapter usado para exibir a lista de materiais na tela de CONFIRMACAO.
 *
 * Cada item mostra:
 *  - Codigo e Descricao do material
 *  - Quantidade preenchida pelo usuario
 */
public class MaterialResumoAdapter extends RecyclerView.Adapter<MaterialResumoAdapter.ViewHolder> {

    private final List<Material> materiais;

    public MaterialResumoAdapter(List<Material> materiais) {
        this.materiais = materiais;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_material_resumo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Material material = materiais.get(position);

        holder.txtCodigo.setText("Codigo: " + material.codigo);
        holder.txtDescricao.setText("Descricao: " + material.descricao);
        holder.txtQuantidade.setText("Quantidade: " + material.quantidade);
    }

    @Override
    public int getItemCount() {
        return materiais != null ? materiais.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtDescricao, txtQuantidade, txtCodigo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCodigo = itemView.findViewById(R.id.txtCodigoResumo);
            txtDescricao = itemView.findViewById(R.id.txtDescricaoResumo);
            txtQuantidade = itemView.findViewById(R.id.txtQuantidadeResumo);
        }
    }
}
