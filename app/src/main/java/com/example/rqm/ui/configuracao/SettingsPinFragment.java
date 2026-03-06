package com.example.rqm.ui.configuracao;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.rqm.R;
import com.example.rqm.utils.AuthPrefs;
import com.example.rqm.utils.SupabaseEdgeClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.Executors;

public class SettingsPinFragment extends Fragment {

    private TextInputLayout layoutPin;
    private TextInputEditText etPin;
    private MaterialButton btnAcessar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_pin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        layoutPin = view.findViewById(R.id.layoutAdminPin);
        etPin = view.findViewById(R.id.etAdminPin);
        btnAcessar = view.findViewById(R.id.btnAdminAcessar);

        if (!AuthPrefs.isLoggedIn(requireContext())) {
            Navigation.findNavController(view).navigate(R.id.nav_loginFragment);
            return;
        }

        if (!AuthPrefs.isAdmin(requireContext())) {
            Toast.makeText(requireContext(), "Acesso restrito a administradores.", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).navigate(R.id.nav_home);
            return;
        }

        btnAcessar.setOnClickListener(v -> validarPin(view));
    }

    private void validarPin(View view) {
        String pin = etPin.getText() != null ? etPin.getText().toString().trim() : "";
        layoutPin.setError(null);

        if (TextUtils.isEmpty(pin)) {
            layoutPin.setError("Informe o PIN");
            return;
        }

        btnAcessar.setEnabled(false);
        Executors.newSingleThreadExecutor().execute(() -> {
            boolean ok = SupabaseEdgeClient.verifyAdminPin(requireContext(), AuthPrefs.getUserId(requireContext()), pin);
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                btnAcessar.setEnabled(true);
                if (ok) {
                    Navigation.findNavController(view).navigate(R.id.nav_configuracao);
                } else {
                    Toast.makeText(requireContext(), "PIN inválido.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
