package com.example.rqm.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rqm.R;
import com.example.rqm.models.Material;
import com.example.rqm.models.Requisicao;
import com.example.rqm.utils.DateUtils;
import com.example.rqm.utils.OperacaoTipo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class HistoricoAdapter extends RecyclerView.Adapter<HistoricoAdapter.ViewHolder> {

    private final List<MaterialComRequisicao> itens = new ArrayList<>();

    public HistoricoAdapter(List<Requisicao> requisicoes) {
        for (Requisicao r : requisicoes) {
            List<Material> materiais = new Gson().fromJson(
                    r.materiaisJson, new TypeToken<List<Material>>() {}.getType());

            for (Material m : materiais) {
                itens.add(new MaterialComRequisicao(r, m));
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historico, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MaterialComRequisicao item = itens.get(position);

        holder.tvProjeto.setText("Projeto: " + safe(item.requisicao.projeto));
        holder.tvRequisitor.setText("Requisitor: " + safe(item.requisicao.requisitor));
        holder.tvCodigo.setText("Código: " + safe(item.material.codigo));
        holder.tvDescricao.setText("Descrição: " + safe(item.material.descricao));
        holder.tvData.setText("Data: " + DateUtils.toDisplayDate(item.requisicao.data));
        holder.tvOperacao.setText("Operação: " + OperacaoTipo.toLabel(item.requisicao.tipoOperacao));

        holder.tvQuantidade.setText("Quantidade: " + item.material.quantidade);
        holder.tvValor.setText("Valor: " + safe(item.material.valor_unitario));

        holder.tvLP.setText("LP: " + (item.material.lp != null && !item.material.lp.isEmpty() ? item.material.lp : "N/A"));
        holder.tvSerial.setText("Serial: " + (item.material.serial != null && !item.material.serial.isEmpty() ? item.material.serial : "N/A"));

        double quantidade = 0;
        double valorUnitario = 0;
        try {
            quantidade = Double.parseDouble(String.valueOf(item.material.quantidade));
        } catch (NumberFormatException ignored) {
        }

        try {
            valorUnitario = Double.parseDouble(item.material.valor_unitario);
        } catch (Exception ignored) {
        }

        double total = valorUnitario > 0 ? quantidade * valorUnitario : quantidade;
        holder.tvTotal.setText("Valor Total: R$ " + String.format("%.2f", total));
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class MaterialComRequisicao {
        Requisicao requisicao;
        Material material;

        MaterialComRequisicao(Requisicao requisicao, Material material) {
            this.requisicao = requisicao;
            this.material = material;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProjeto, tvRequisitor, tvCodigo, tvDescricao,
                tvQuantidade, tvValor, tvData, tvOperacao, tvLP, tvSerial, tvTotal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProjeto = itemView.findViewById(R.id.tvProjeto);
            tvRequisitor = itemView.findViewById(R.id.tvRequisitor);
            tvCodigo = itemView.findViewById(R.id.tvCodigo);
            tvDescricao = itemView.findViewById(R.id.tvDescricao);
            tvQuantidade = itemView.findViewById(R.id.tvQuantidade);
            tvValor = itemView.findViewById(R.id.tvValor);
            tvData = itemView.findViewById(R.id.tvData);
            tvLP = itemView.findViewById(R.id.tvLP);
            tvSerial = itemView.findViewById(R.id.tvSerial);
            tvOperacao = itemView.findViewById(R.id.tvOperacao);
            tvTotal = itemView.findViewById(R.id.tvTotal);
        }
    }

    private String safe(String valor) {
        return valor != null ? valor : "";
    }

    public void setData(List<Requisicao> requisicoes) {
        itens.clear();
        for (Requisicao r : requisicoes) {
            List<Material> materiais = new Gson().fromJson(
                    r.materiaisJson, new TypeToken<List<Material>>() {}.getType());
            for (Material m : materiais) {
                itens.add(new MaterialComRequisicao(r, m));
            }
        }
        notifyDataSetChanged();
    }
}
