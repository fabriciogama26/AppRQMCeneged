package com.example.rqm.adapters;

// Importações necessárias
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
 * Adapter usado para exibir a lista de materiais na tela de CONFIRMAÇÃO.
 *
 * Ligação:
 * ┌──────────────────────────────┐
 * │ ConfirmacaoFragment          │
 * │      ↳ RecyclerView          │
 * │           ↳ MaterialResumoAdapter       │
 * │                ↳ item_material_resumo.xml │
 * └──────────────────────────────┘
 *
 * Cada item mostra:
 *  - Código e Descrição do material
 *  - Quantidade preenchida pelo usuário
 *  - Informações adicionais: UMD, Tipo, Preço
 *  - LP e Serial (se forem aplicáveis ao material)
 */
public class MaterialResumoAdapter extends RecyclerView.Adapter<MaterialResumoAdapter.ViewHolder> {

    // Lista de materiais que será exibida no RecyclerView
    private final List<Material> materiais;

    // Construtor: recebe a lista de materiais selecionados
    public MaterialResumoAdapter(List<Material> materiais) {
        this.materiais = materiais;
    }

    /**
     * Cria a visualização (view) de cada item da lista
     * Inflando o layout item_material_resumo.xml
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_material_resumo, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Associa os dados do material com a interface (TextViews)
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Material material = materiais.get(position);

        // Informações principais do material
        holder.txtCodigo.setText("Código: " + material.codigo);
        holder.txtDescricao.setText("Descrição: " + material.descricao);
        holder.txtQuantidade.setText("Quantidade: " + material.quantidade);
        holder.txtPreço.setText("Preço: " + material.valor_unitario);
        holder.txtUmb.setText("UMD: " + material.umb);
        holder.txtTipo.setText("Tipo: " + material.tipo);

        // Informações adicionais: LP e Serial (podem ser nulos ou vazios)
        holder.txtLP.setText("LP: " + (material.lp != null && !material.lp.isEmpty() ? material.lp : "N/A"));
        holder.txtSerial.setText("Serial: " + (material.serial != null && !material.serial.isEmpty() ? material.serial : "N/A"));
    }

    /**
     * Retorna o número de itens (evita NullPointerException)
     */
    @Override
    public int getItemCount() {
        return materiais != null ? materiais.size() : 0;
    }

    /**
     * Classe interna que representa cada item da lista no RecyclerView
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Campos visuais que serão preenchidos com os dados do Material
        TextView txtDescricao, txtQuantidade, txtPreço, txtUmb,
                txtCodigo, txtTipo, txtLP, txtSerial;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Conecta os TextViews aos elementos do XML (item_material_resumo.xml)
            txtCodigo = itemView.findViewById(R.id.txtCodigoResumo);
            txtDescricao = itemView.findViewById(R.id.txtDescricaoResumo);
            txtQuantidade = itemView.findViewById(R.id.txtQuantidadeResumo);
            txtPreço = itemView.findViewById(R.id.txtPeçoResumo);
            txtUmb = itemView.findViewById(R.id.txtUmbResumo);
            txtTipo = itemView.findViewById(R.id.txtTipoResumo);
            txtLP = itemView.findViewById(R.id.txtLPResumo);
            txtSerial = itemView.findViewById(R.id.txtSerialResumo);
        }
    }
}
