package com.example.rqm.ui.configuracao;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.rqm.R;
import com.example.rqm.utils.SupabasePrefs;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SupabaseConfigFragment extends Fragment {

    private TextInputLayout layoutUrl;
    private TextInputLayout layoutAnon;
    private TextInputEditText etUrl;
    private TextInputEditText etAnon;
    private TextView tvStatus;
    private View statusDot;
    private MaterialButton btnConectar;
    private MaterialButton btnDesconectar;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_supabase_config, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        com.example.rqm.utils.SupabaseConfigLoader.ensureConfig(requireContext());

        layoutUrl = view.findViewById(R.id.layoutSupabaseUrl);
        layoutAnon = view.findViewById(R.id.layoutSupabaseAnon);
        etUrl = view.findViewById(R.id.etSupabaseUrl);
        etAnon = view.findViewById(R.id.etSupabaseAnon);
        tvStatus = view.findViewById(R.id.tvSupabaseStatus);
        statusDot = view.findViewById(R.id.viewSupabaseDot);
        btnConectar = view.findViewById(R.id.btnSupabaseConectar);
        btnDesconectar = view.findViewById(R.id.btnSupabaseDesconectar);

        etUrl.setText(SupabasePrefs.getUrl(requireContext()));
        etAnon.setText(SupabasePrefs.getAnonKey(requireContext()));

        atualizarStatus(SupabasePrefs.getStatus(requireContext()));

        btnConectar.setOnClickListener(v -> conectar());
        btnDesconectar.setOnClickListener(v -> desconectar());
    }

    private void conectar() {
        String url = safeUrl(etUrl.getText() != null ? etUrl.getText().toString().trim() : "");
        String anon = etAnon.getText() != null ? etAnon.getText().toString().trim() : "";

        layoutUrl.setError(null);
        layoutAnon.setError(null);

        if (TextUtils.isEmpty(url)) {
            layoutUrl.setError("Informe a URL do projeto");
            return;
        }
        if (TextUtils.isEmpty(anon)) {
            layoutAnon.setError("Informe a anon key");
            return;
        }

        SupabasePrefs.saveConfig(requireContext(), url, anon);
        SupabasePrefs.setStatus(requireContext(), SupabasePrefs.STATUS_CONNECTING);
        atualizarStatus(SupabasePrefs.STATUS_CONNECTING);
        requireActivity().invalidateOptionsMenu();

        btnConectar.setEnabled(false);

        executor.execute(() -> {
            boolean ok = testarConexao(url, anon);
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    btnConectar.setEnabled(true);
                    SupabasePrefs.setStatus(requireContext(),
                            ok ? SupabasePrefs.STATUS_CONNECTED : SupabasePrefs.STATUS_DISCONNECTED);
                    atualizarStatus(SupabasePrefs.getStatus(requireContext()));
                    requireActivity().invalidateOptionsMenu();
                    Toast.makeText(requireContext(),
                            ok ? "Conectado ao Supabase" : "Falha ao conectar",
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void desconectar() {
        SupabasePrefs.setStatus(requireContext(), SupabasePrefs.STATUS_DISCONNECTED);
        atualizarStatus(SupabasePrefs.STATUS_DISCONNECTED);
        requireActivity().invalidateOptionsMenu();
        Toast.makeText(requireContext(), "Desconectado", Toast.LENGTH_SHORT).show();
    }

    private boolean testarConexao(String baseUrl, String anonKey) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(baseUrl + "/auth/v1/health");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("apikey", anonKey);
            connection.setRequestProperty("Authorization", "Bearer " + anonKey);
            int code = connection.getResponseCode();
            return code >= 200 && code < 300;
        } catch (IOException ignored) {
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String safeUrl(String url) {
        if (TextUtils.isEmpty(url)) return "";
        String fixed = url.trim();
        if (fixed.endsWith("/")) {
            fixed = fixed.substring(0, fixed.length() - 1);
        }
        if (!fixed.startsWith("http://") && !fixed.startsWith("https://")) {
            fixed = "https://" + fixed;
        }
        return fixed;
    }

    private void atualizarStatus(int status) {
        int color;
        String text;
        if (status == SupabasePrefs.STATUS_CONNECTED) {
            color = ContextCompat.getColor(requireContext(), R.color.status_green);
            text = "Status: conectado";
        } else if (status == SupabasePrefs.STATUS_CONNECTING) {
            color = ContextCompat.getColor(requireContext(), R.color.status_yellow);
            text = "Status: conectando";
        } else {
            color = ContextCompat.getColor(requireContext(), R.color.status_red);
            text = "Status: desconectado";
        }

        statusDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
        tvStatus.setText(text);
    }

    @Override
    public void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
