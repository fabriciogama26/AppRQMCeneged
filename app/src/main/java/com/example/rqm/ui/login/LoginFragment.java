package com.example.rqm.ui.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.rqm.R;
import com.example.rqm.utils.UsuarioManager;

public class LoginFragment extends Fragment {

    private EditText etUsuario, etSenha;
    private Button btnEntrar;
    private ImageView imgLogo;
    private LoginViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etUsuario = view.findViewById(R.id.etUsuario);
        etSenha = view.findViewById(R.id.etSenha);
        btnEntrar = view.findViewById(R.id.btnEntrar);
        imgLogo = view.findViewById(R.id.imgLogo);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        btnEntrar.setOnClickListener(v -> {
            String usuario = etUsuario.getText().toString().trim();
            String senha = etSenha.getText().toString();

            if (TextUtils.isEmpty(usuario) || TextUtils.isEmpty(senha)) {
                Toast.makeText(getContext(), "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
                return;
            }

            UsuarioManager usuarioManager = new UsuarioManager(requireContext());
            if (usuarioManager.validarLogin(usuario, senha)) {
                Navigation.findNavController(view).navigate(R.id.nav_configuracao);
            } else {
                Toast.makeText(getContext(), "Usuário ou senha incorretos.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
