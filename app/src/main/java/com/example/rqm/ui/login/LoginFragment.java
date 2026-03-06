package com.example.rqm.ui.login;

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

import com.example.rqm.MainActivity;
import com.example.rqm.R;
import com.example.rqm.utils.AuthPrefs;
import com.example.rqm.utils.SupabaseEdgeClient;
import com.example.rqm.utils.TestModeConfig;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.Executors;

public class LoginFragment extends Fragment {

    private TextInputLayout layoutMatricula;
    private TextInputLayout layoutSenha;
    private TextInputEditText etUsuario;
    private TextInputEditText etSenha;
    private MaterialButton btnEntrar;
    private MaterialButton btnSobre;
    private View tvTestModeInfo;
    private TestModeConfig testModeConfig;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        layoutMatricula = view.findViewById(R.id.layoutMatricula);
        layoutSenha = view.findViewById(R.id.layoutSenha);
        etUsuario = view.findViewById(R.id.etUsuario);
        etSenha = view.findViewById(R.id.etSenha);
        btnEntrar = view.findViewById(R.id.btnEntrar);
        btnSobre = view.findViewById(R.id.btnSobre);
        tvTestModeInfo = view.findViewById(R.id.tvTestModeInfo);
        testModeConfig = TestModeConfig.load(requireContext());

        aplicarModoTeste();

        btnEntrar.setOnClickListener(v -> tentarLogin(view));
        btnSobre.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.nav_sobre_login));
    }

    private void aplicarModoTeste() {
        if (!testModeConfig.enabled) {
            return;
        }

        layoutSenha.setVisibility(View.GONE);
        tvTestModeInfo.setVisibility(View.VISIBLE);
        btnEntrar.setText(R.string.btn_entrar_teste);
        etUsuario.setText(testModeConfig.matricula);
        etUsuario.setEnabled(false);
        etUsuario.setFocusable(false);
        etUsuario.setClickable(false);
    }

    private void tentarLogin(View view) {
        if (testModeConfig.enabled) {
            entrarModoTeste(view);
            return;
        }

        String matricula = etUsuario.getText() != null ? etUsuario.getText().toString().trim() : "";
        String senha = etSenha.getText() != null ? etSenha.getText().toString().trim() : "";
        boolean imeiValidado = AuthPrefs.isImeiValidated(requireContext());
        String imei = AuthPrefs.getImei(requireContext());

        layoutMatricula.setError(null);
        layoutSenha.setError(null);

        if (TextUtils.isEmpty(matricula)) {
            layoutMatricula.setError(getString(R.string.erro_matricula));
            return;
        }
        if (TextUtils.isEmpty(senha)) {
            layoutSenha.setError(getString(R.string.erro_senha));
            return;
        }
        if (TextUtils.isEmpty(imei)) {
            Toast.makeText(requireContext(), getString(R.string.msg_imei_obrigatorio), Toast.LENGTH_SHORT).show();
            return;
        }

        btnEntrar.setEnabled(false);

        boolean skipImeiCheck = imeiValidado;
        Executors.newSingleThreadExecutor().execute(() -> {
            SupabaseEdgeClient.LoginResult result = SupabaseEdgeClient.login(
                    requireContext(), matricula, senha, imei, skipImeiCheck);
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                btnEntrar.setEnabled(true);
                if (result.success) {
                    AuthPrefs.saveLogin(requireContext(), result.accessToken, result.userId,
                            matricula, result.role, result.tenantId, result.loginAuditId);
                    if (!imeiValidado) {
                        AuthPrefs.setImei(requireContext(), imei, true);
                    }
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("auto_sync", true);
                    MainActivity.navigateToHome(Navigation.findNavController(view), bundle);
                } else {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void entrarModoTeste(View view) {
        String imei = testModeConfig.imei;
        if (TextUtils.isEmpty(imei)) {
            imei = "IMEI-TESTE";
        }

        AuthPrefs.setImei(requireContext(), imei, true);
        AuthPrefs.saveLogin(
                requireContext(),
                AuthPrefs.TEST_MODE_TOKEN,
                testModeConfig.userId,
                testModeConfig.matricula,
                testModeConfig.role,
                testModeConfig.tenantId,
                "test-mode-audit"
        );
        MainActivity.navigateToHome(Navigation.findNavController(view), null);
    }
}
