package com.example.rqm;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.rqm.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    // Código da solicitação de permissão
    private static final int REQUEST_STORAGE_PERMISSION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Infla o layout usando View Binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configura toolbar como ActionBar
        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Define os destinos de topo para controle de navegação (IDs devem existir no menu/navigation)
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,
                R.id.nav_materiais,
                R.id.nav_historico,
                R.id.nav_configuracao,
                R.id.nav_sobre
        ).setOpenableLayout(drawer).build();

        // Controlador de navegação
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        // Sincroniza o ActionBar e Navigation Drawer com NavController
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Atualiza o título da toolbar dinamicamente para Requisição ou Devolução
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.nav_operacao && arguments != null) {
                String tipo = arguments.getString("tipo_operacao", "");
                if (tipo.equalsIgnoreCase("Devolução")) {
                    binding.appBarMain.toolbar.setTitle("Devolução");
                } else {
                    binding.appBarMain.toolbar.setTitle("Requisição");
                }
            } else {
                // Define o título padrão para outras telas
                binding.appBarMain.toolbar.setTitle(destination.getLabel());
            }
        });

        // Verifica permissões de armazenamento, se necessário
        verificarPermissoesArmazenamento();
    }

    // Infla o menu superior (3 pontinhos), se existir
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // Gerencia a navegação do botão "voltar" no topo
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    /**
     * Verifica e solicita permissões de armazenamento externo (Android 6 até 10).
     * Android 11+ já permite exportação via getExternalFilesDir sem permissão.
     */
    private void verificarPermissoesArmazenamento() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            // Para Android < 11, ainda é necessário solicitar WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
            }
        } else {
            // Android 11+ não requer permissão para getExternalFilesDir()
            // Você pode exibir um aviso ao usuário, se quiser
            Toast.makeText(this, "Permissão não necessária no Android 11+", Toast.LENGTH_SHORT).show();
        }
    }

    // Trata o resultado da solicitação de permissão
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissão de armazenamento concedida", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissão negada. Algumas funções podem não funcionar", Toast.LENGTH_LONG).show();
            }
        }
    }
}
