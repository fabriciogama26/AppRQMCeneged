package com.example.rqm.ui.Home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.rqm.R;

public class HomeFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        Button btnRequisicao = root.findViewById(R.id.btnRequisicao);
        Button btnDevolucao = root.findViewById(R.id.btnDevolucao);

        btnRequisicao.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putString("tipo_operacao", "Requisição");
            Navigation.findNavController(view).navigate(R.id.nav_operacao, bundle); // ✅ Correto
        });

        btnDevolucao.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putString("tipo_operacao", "Devolução");
            Navigation.findNavController(view).navigate(R.id.nav_operacao, bundle); // ✅ Correto
        });

        return root;
    }
}
