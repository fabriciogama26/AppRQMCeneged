package com.example.rqm;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.NavOptions;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.rqm.databinding.ActivityMainBinding;
import com.example.rqm.utils.AuthPrefs;
import com.example.rqm.utils.ErrorLogger;
import com.example.rqm.utils.OperacaoTipo;
import com.example.rqm.utils.SupabaseConfigLoader;
import com.example.rqm.utils.SupabaseEdgeClient;
import com.example.rqm.utils.SupabasePrefs;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private int currentDestinationId = 0;

    private static final int REQUEST_STORAGE_PERMISSION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,
                R.id.nav_materiais,
                R.id.nav_historico,
                R.id.nav_sync_runs,
                R.id.nav_settings_pin,
                R.id.nav_sobre
        ).setOpenableLayout(drawer).build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_logout) {
                drawer.closeDrawer(GravityCompat.START);
                performLogout("USER_LOGOUT");
                return true;
            }
            if (item.getItemId() == R.id.nav_estoque) {
                NavOptions options = new NavOptions.Builder()
                        .setLaunchSingleTop(true)
                        .setPopUpTo(R.id.nav_home, false)
                        .build();
                navController.navigate(R.id.nav_estoque, null, options);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
            boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
            if (handled) {
                drawer.closeDrawer(GravityCompat.START);
            }
            return handled;
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            currentDestinationId = destination.getId();
            invalidateOptionsMenu();
            if (destination.getId() == R.id.nav_operacao && arguments != null) {
                String tipo = arguments.getString("tipo_operacao", "");
                binding.appBarMain.toolbar.setTitle(getString(OperacaoTipo.toLabelResId(tipo)));
            } else {
                binding.appBarMain.toolbar.setTitle(destination.getLabel());
            }

            boolean isLoginScreen = destination.getId() == R.id.nav_loginFragment;
            drawer.setDrawerLockMode(isLoginScreen
                    ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED
                    : DrawerLayout.LOCK_MODE_UNLOCKED);
            if (isLoginScreen) {
                binding.appBarMain.toolbar.setNavigationIcon(null);
            }
        });

        verificarPermissoesArmazenamento();

        SupabaseConfigLoader.ensureConfig(this);

        if (AuthPrefs.isLoggedIn(this) && !AuthPrefs.shouldExpireByDate(this)) {
            navigateToHome(navController, null);
        } else {
            navigateToLogin(navController);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        atualizarStatusBanco(menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        atualizarStatusBanco(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
        ErrorLogger.flushPending(this);

        String pendingReason = AuthPrefs.consumePendingLogout(this);
        if (!pendingReason.isEmpty() && AuthPrefs.isLoggedIn(this)) {
            String auditId = AuthPrefs.getLoginAuditId(this);
            if (!auditId.isEmpty() && !AuthPrefs.isTestSession(this)) {
                SupabaseEdgeClient.logout(this, auditId, pendingReason);
            }
            AuthPrefs.clearSession(this);
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            navigateToLogin(navController);
            return;
        }

        if (AuthPrefs.isLoggedIn(this) && AuthPrefs.shouldExpireByDate(this)) {
            performLogout("SESSION_EXPIRED_DATE");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void verificarPermissoesArmazenamento() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
            }
        } else {
            Toast.makeText(this, "Permissao nao necessaria no Android 11+.", Toast.LENGTH_SHORT).show();
        }
    }

    private void atualizarStatusBanco(Menu menu) {
        MenuItem statusItem = menu.findItem(R.id.action_db_status);
        if (statusItem == null) return;
        View actionView = statusItem.getActionView();
        if (actionView == null) return;
        View dot = actionView.findViewById(R.id.dbStatusDot);
        android.widget.TextView statusText = actionView.findViewById(R.id.dbStatusText);
        if (dot == null) return;

        int status = SupabasePrefs.getStatus(this);
        int color;
        String text;
        if (status == SupabasePrefs.STATUS_CONNECTED) {
            color = getColor(R.color.status_green);
            text = "Conectado";
        } else if (status == SupabasePrefs.STATUS_CONNECTING) {
            color = getColor(R.color.status_yellow);
            text = "Conectando";
        } else {
            color = getColor(R.color.status_red);
            text = "Desconectado";
        }
        dot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
        if (statusText != null) {
            statusText.setText(text);
            boolean showText = currentDestinationId == R.id.nav_home;
            statusText.setVisibility(showText ? View.VISIBLE : View.GONE);
        }
    }

    private void performLogout(String reason) {
        String auditId = AuthPrefs.getLoginAuditId(this);
        if (!auditId.isEmpty() && !AuthPrefs.isTestSession(this)) {
            SupabaseEdgeClient.logout(this, auditId, reason);
        }
        AuthPrefs.clearSession(this);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        navigateToLogin(navController);
    }

    public static void navigateToHome(@NonNull NavController navController, Bundle args) {
        NavOptions options = new NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setPopUpTo(R.id.nav_loginFragment, true)
                .build();
        navController.navigate(R.id.nav_home, args, options);
    }

    private void navigateToLogin(@NonNull NavController navController) {
        NavOptions options = new NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setPopUpTo(navController.getGraph().getStartDestinationId(), true)
                .build();
        navController.navigate(R.id.nav_loginFragment, null, options);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissao de armazenamento concedida.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissao negada. Algumas funcoes podem nao funcionar.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
