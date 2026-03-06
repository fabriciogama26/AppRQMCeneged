package com.example.rqm.ui.formulario_operacao;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.rqm.R;
import com.example.rqm.data.ResponsavelDao;
import com.example.rqm.utils.AuthPrefs;
import com.example.rqm.utils.DateUtils;
import com.example.rqm.utils.OperacaoTipo;
import com.example.rqm.utils.ResponsavelPrefs;

import java.util.List;

public class FormularioOperacaoFragment extends Fragment {

    private FormularioOperacaoViewModel viewModel;
    private Spinner spinnerPrefixo;
    private EditText etProjetoNumero;
    private EditText etData;
    private EditText etUsuario;
    private AutoCompleteTextView etRequisitor;
    private Button btnAvancar;
    private boolean isFormatting = false;
    private String tipoOperacao = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_formulario_operacao, container, false);

        spinnerPrefixo = view.findViewById(R.id.spinnerPrefixo);
        etProjetoNumero = view.findViewById(R.id.etProjetoNumero);
        etData = view.findViewById(R.id.etData);
        etRequisitor = view.findViewById(R.id.etRequisitor);
        etUsuario = view.findViewById(R.id.etUsuario);
        btnAvancar = view.findViewById(R.id.btnAvancar);

        viewModel = new ViewModelProvider(this).get(FormularioOperacaoViewModel.class);

        Bundle args = getArguments();
        if (args != null) {
            tipoOperacao = args.getString("tipo_operacao", OperacaoTipo.REQ);
        }

        if (getActivity() instanceof AppCompatActivity) {
            Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
            if (toolbar != null) {
                toolbar.setTitle(getString(OperacaoTipo.toLabelResId(tipoOperacao)));
            }
        }

        atualizarCampoData();
        preencherAlmoxarife();
        configurarResponsavel();

        etData.setOnClickListener(v -> viewModel.abrirDatePicker(requireContext(), selecionarData));

        btnAvancar.setOnClickListener(v -> {
            if (!validarCampos()) return;

            String requisitor = etRequisitor.getText() != null
                    ? etRequisitor.getText().toString().trim().toUpperCase()
                    : "";
            String usuario = etUsuario.getText() != null ? etUsuario.getText().toString().trim() : "";
            String prefixo = spinnerPrefixo.getSelectedItem().toString();
            String numeroProjeto = etProjetoNumero.getText().toString().trim();
            String projeto = prefixo + "-" + numeroProjeto;
            String dataDisplay = etData.getText().toString().trim();
            String dataIso = DateUtils.toIsoWithNow(dataDisplay);

            ResponsavelPrefs.addResponsavel(requireContext(), requisitor);

            Bundle bundle = new Bundle();
            bundle.putString("requisitor", requisitor);
            bundle.putString("usuario", usuario);
            bundle.putString("projeto", projeto);
            bundle.putString("data", dataDisplay);
            bundle.putString("data_display", dataDisplay);
            bundle.putString("data_iso", dataIso);
            bundle.putString("tipoOperacao", tipoOperacao);

            Navigation.findNavController(v).navigate(R.id.nav_materiais, bundle);
        });

        etProjetoNumero.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                if (isFormatting) return;
                isFormatting = true;
                String formatado = viewModel.formatarNumeroProjeto(
                        spinnerPrefixo.getSelectedItem().toString(),
                        s.toString()
                );
                etProjetoNumero.setText(formatado);
                etProjetoNumero.setSelection(formatado.length());
                isFormatting = false;
            }
        });

        return view;
    }

    private void preencherAlmoxarife() {
        String matricula = AuthPrefs.getMatricula(requireContext());
        etUsuario.setText(matricula != null ? matricula.toUpperCase() : "");
    }

    private void configurarResponsavel() {
        List<String> nomes = new ResponsavelDao(requireContext()).listarNomes();
        if (nomes.isEmpty()) {
            nomes = ResponsavelPrefs.getResponsaveis(requireContext());
        }
        final List<String> responsaveis = nomes;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                responsaveis
        );
        etRequisitor.setAdapter(adapter);
        etRequisitor.setOnClickListener(v -> {
            if (!responsaveis.isEmpty()) {
                etRequisitor.showDropDown();
            }
        });
        etRequisitor.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && !responsaveis.isEmpty()) {
                etRequisitor.showDropDown();
            }
        });
    }

    private void atualizarCampoData() {
        etData.setText(viewModel.getDataAtualFormatada());
    }

    private final DatePickerDialog.OnDateSetListener selecionarData = (view, year, month, dayOfMonth) -> {
        viewModel.setData(year, month, dayOfMonth);
        atualizarCampoData();
    };

    private boolean validarCampos() {
        String prefixo = spinnerPrefixo.getSelectedItem().toString();
        String numeroProjeto = etProjetoNumero.getText().toString().trim();
        String requisitor = etRequisitor.getText() != null ? etRequisitor.getText().toString().trim() : "";
        String usuario = etUsuario.getText() != null ? etUsuario.getText().toString().trim() : "";

        boolean valido = viewModel.validarCampos(prefixo, numeroProjeto, requisitor, usuario);

        if (!valido) {
            if (numeroProjeto.isEmpty() || requisitor.isEmpty() || usuario.isEmpty()) {
                Toast.makeText(getContext(), "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Formato de projeto invalido.", Toast.LENGTH_SHORT).show();
            }
        }

        return valido;
    }
}
