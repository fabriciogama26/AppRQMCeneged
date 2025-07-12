package com.example.rqm.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rqm.R;
import com.example.rqm.models.Material;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MaterialAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnMaterialExcluirListener {
        void onExcluir(Material material);
    }

    private List<Material> materialList;
    private final OnMaterialExcluirListener excluirListener;
    private final Set<String> codigosComLP;

    public MaterialAdapter(List<Material> materials, OnMaterialExcluirListener listener, Set<String> codigosComLP) {
        this.materialList = materials;
        this.excluirListener = listener;
        this.codigosComLP = codigosComLP != null ? codigosComLP : new HashSet<>();
    }

    // ViewHolder padrão
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtCodigo, txtDescricao, txtTipo, txtPreco, txtUmb;
        EditText txtQuantidade;
        Button btnExcluir;

        public ViewHolder(View view) {
            super(view);
            txtCodigo = view.findViewById(R.id.txtCodigo);
            txtDescricao = view.findViewById(R.id.txtDescricao);
            txtTipo = view.findViewById(R.id.txtTipo);
            txtPreco = view.findViewById(R.id.txtPreco);
            txtUmb = view.findViewById(R.id.txtUmb);
            txtQuantidade = view.findViewById(R.id.edtQuantidade);
            btnExcluir = view.findViewById(R.id.btnExcluir);
        }

        public void bind(Material material, OnMaterialExcluirListener listener) {
            txtCodigo.setText("Código: " + material.codigo);
            txtDescricao.setText(material.descricao);
            txtQuantidade.setText(material.quantidade > 0 ? String.valueOf(material.quantidade) : "");
            txtUmb.setText("UMB: " + material.umb);
            txtPreco.setText("Preço Unitário: R$ " + material.valor_unitario);
            txtTipo.setText("Tipo: " + material.tipo);

            txtQuantidade.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    String valor = txtQuantidade.getText().toString().trim();
                    try {
                        material.quantidade = valor.isEmpty() ? 0 : Integer.parseInt(valor);
                    } catch (NumberFormatException e) {
                        material.quantidade = 0;
                        txtQuantidade.setText("0");
                    }
                }
            });

            btnExcluir.setOnClickListener(v -> {
                if (listener != null) listener.onExcluir(material);
            });
        }
    }

    // ViewHolder LP com campos extras
    public static class ViewHolderLP extends RecyclerView.ViewHolder {
        TextView txtCodigo, txtDescricao, txtTipo, txtPreco, txtUmb;
        EditText txtQuantidade, txtLP, txtSerial;
        Button btnExcluir;

        public ViewHolderLP(View view) {
            super(view);
            txtCodigo = view.findViewById(R.id.txtCodigo);
            txtDescricao = view.findViewById(R.id.txtDescricao);
            txtTipo = view.findViewById(R.id.txtTipo);
            txtPreco = view.findViewById(R.id.txtPreco);
            txtUmb = view.findViewById(R.id.txtUmb);
            txtQuantidade = view.findViewById(R.id.edtQuantidade);
            txtLP = view.findViewById(R.id.edtLP);
            txtSerial = view.findViewById(R.id.edtSerial);
            btnExcluir = view.findViewById(R.id.btnExcluir);
        }

        public void bind(Material material, OnMaterialExcluirListener listener) {
            txtCodigo.setText("Código: " + material.codigo);
            txtDescricao.setText(material.descricao);
            txtQuantidade.setText(material.quantidade > 0 ? String.valueOf(material.quantidade) : "");
            txtUmb.setText("UMB: " + material.umb);
            txtPreco.setText("Preço Unitário: R$ " + material.valor_unitario);
            txtTipo.setText("Tipo: " + material.tipo);
            txtLP.setText(material.lp);
            txtSerial.setText(material.serial);

            txtQuantidade.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    String valor = txtQuantidade.getText().toString().trim();
                    material.quantidade = valor.isEmpty() ? 0 : Integer.parseInt(valor);
                }
            });

            txtLP.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) material.lp = txtLP.getText().toString().trim();
            });

            txtSerial.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) material.serial = txtSerial.getText().toString().trim();
            });

            btnExcluir.setOnClickListener(v -> {
                if (listener != null) listener.onExcluir(material);
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        Material material = materialList.get(position);
        return codigosComLP.contains(material.codigo) ? 1 : 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == 1) {
            View view = inflater.inflate(R.layout.item_material_lp, parent, false);
            return new ViewHolderLP(view);
        } else {
            View view = inflater.inflate(R.layout.item_material, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Material material = materialList.get(position);
        if (holder instanceof ViewHolderLP) {
            ((ViewHolderLP) holder).bind(material, excluirListener);
        } else if (holder instanceof ViewHolder) {
            ((ViewHolder) holder).bind(material, excluirListener);
        }
    }

    @Override
    public int getItemCount() {
        return materialList.size();
    }

    public List<Material> getMaterialList() {
        return materialList;
    }

    public void setMaterialList(List<Material> novosMateriais) {
        for (Material novo : novosMateriais) {
            for (Material antigo : this.materialList) {
                if (antigo.codigo.equals(novo.codigo)) {
                    novo.quantidade = antigo.quantidade;
                    novo.lp = antigo.lp;
                    novo.serial = antigo.serial;
                    break;
                }
            }
        }
        this.materialList = novosMateriais;
        notifyDataSetChanged();
    }
}
