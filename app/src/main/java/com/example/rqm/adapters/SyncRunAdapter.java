package com.example.rqm.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rqm.R;
import com.example.rqm.models.SyncRun;
import com.example.rqm.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class SyncRunAdapter extends RecyclerView.Adapter<SyncRunAdapter.ViewHolder> {

    private final List<SyncRun> items = new ArrayList<>();

    public void setItems(List<SyncRun> runs) {
        items.clear();
        if (runs != null) {
            items.addAll(runs);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sync_run, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SyncRun run = items.get(position);
        holder.tvStatus.setText("Status: " + safe(run.status));
        holder.tvDate.setText("Início: " + formatDate(run.startedAt));
        holder.tvResumo.setText(run.pendingSent + " pendências enviadas | " + run.materialsUpdated + " itens");
        holder.tvConflitos.setText("Conflitos: " + run.conflictsFound + " | Erros: " + run.errorsCount);
        holder.tvMensagem.setText(safe(run.message));
        holder.tvEnvio.setText(run.uploadedToServer ? "Resumo enviado ao SaaS" : "Resumo pendente de envio ao SaaS");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStatus, tvDate, tvResumo, tvConflitos, tvMensagem, tvEnvio;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStatus = itemView.findViewById(R.id.tvSyncStatus);
            tvDate = itemView.findViewById(R.id.tvSyncDate);
            tvResumo = itemView.findViewById(R.id.tvSyncResumo);
            tvConflitos = itemView.findViewById(R.id.tvSyncConflitos);
            tvMensagem = itemView.findViewById(R.id.tvSyncMensagem);
            tvEnvio = itemView.findViewById(R.id.tvSyncEnvio);
        }
    }

    private String formatDate(String value) {
        return DateUtils.toDisplayDate(value) + (value != null && value.contains("T") ? " " + value.substring(11, Math.min(16, value.length())) : "");
    }

    private String safe(String value) {
        return value != null ? value : "";
    }
}