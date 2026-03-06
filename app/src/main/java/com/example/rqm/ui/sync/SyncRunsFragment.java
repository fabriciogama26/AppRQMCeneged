package com.example.rqm.ui.sync;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rqm.R;
import com.example.rqm.adapters.SyncRunAdapter;
import com.example.rqm.data.SyncRunDao;
import com.example.rqm.models.SyncRun;

import java.util.List;

public class SyncRunsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sync_runs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerSyncRuns);
        TextView emptyView = view.findViewById(R.id.tvSyncEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        SyncRunAdapter adapter = new SyncRunAdapter();
        recyclerView.setAdapter(adapter);

        List<SyncRun> runs = new SyncRunDao(requireContext()).listarTodos();
        adapter.setItems(runs);
        boolean empty = runs == null || runs.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}