package com.example.rqm.ui.sobre;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rqm.R;
import com.example.rqm.utils.AuthPrefs;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class SobreLoginFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sobre_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextInputEditText etImei = view.findViewById(R.id.etImeiCadastro);
        MaterialButton btnSalvar = view.findViewById(R.id.btnSalvarImei);

        String imeiSalvo = AuthPrefs.getImei(requireContext());
        if (!TextUtils.isEmpty(imeiSalvo)) {
            etImei.setText(imeiSalvo);
        }

        btnSalvar.setOnClickListener(v -> {
            String imei = etImei.getText() != null ? etImei.getText().toString().trim() : "";
            if (TextUtils.isEmpty(imei)) {
                Toast.makeText(requireContext(), getString(R.string.erro_imei), Toast.LENGTH_SHORT).show();
                return;
            }

            boolean validated = AuthPrefs.isImeiValidated(requireContext());
            String atual = AuthPrefs.getImei(requireContext());
            if (!TextUtils.equals(atual, imei)) {
                validated = false;
            }
            AuthPrefs.setImei(requireContext(), imei, validated);
            Toast.makeText(requireContext(), getString(R.string.msg_imei_salvo), Toast.LENGTH_SHORT).show();
        });
    }
}
