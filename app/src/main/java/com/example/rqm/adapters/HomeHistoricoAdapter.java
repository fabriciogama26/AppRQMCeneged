package com.example.rqm.adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rqm.R;
import com.example.rqm.models.Requisicao;
import com.example.rqm.utils.DateUtils;
import com.example.rqm.utils.OperacaoTipo;

import java.util.List;

public class HomeHistoricoAdapter extends RecyclerView.Adapter<HomeHistoricoAdapter.ViewHolder> {

    private final List<Requisicao> itens;

    public HomeHistoricoAdapter(List<Requisicao> itens) {
        this.itens = itens;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_historico, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Requisicao r = itens.get(position);
        holder.tvData.setText("Data: " + DateUtils.toDisplayDate(r.data));
        holder.tvOperacao.setText(holder.itemView.getContext().getString(OperacaoTipo.toLabelResId(r.tipoOperacao)));
        holder.tvUsuario.setText("Almoxarife: " + safe(r.usuario));
        holder.tvProjeto.setText("Projeto: " + safe(r.projeto));

        String origem = TextUtils.isEmpty(r.origem) ? "APP" : r.origem;
        String device = TextUtils.isEmpty(r.deviceId) ? "N/A" : r.deviceId;
        holder.tvOrigem.setText("Origem: " + origem + " | Dispositivo: " + device);
    }

    @Override
    public int getItemCount() {
        return itens != null ? itens.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvData, tvOperacao, tvUsuario, tvProjeto, tvOrigem;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvData = itemView.findViewById(R.id.tvHomeData);
            tvOperacao = itemView.findViewById(R.id.tvHomeOperacao);
            tvUsuario = itemView.findViewById(R.id.tvHomeUsuario);
            tvProjeto = itemView.findViewById(R.id.tvHomeProjeto);
            tvOrigem = itemView.findViewById(R.id.tvHomeOrigem);
        }
    }

    private String safe(String value) {
        return value != null ? value : "";
    }
}