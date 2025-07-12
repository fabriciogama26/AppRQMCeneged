package com.example.rqm.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rqm.MainActivity;
import com.example.rqm.R;

/**
 * Tela de splash (animação inicial).
 * Após um pequeno atraso, redireciona diretamente para o MainActivity.
 * A verificação de serial foi removida.
 */
public class SplashActivity extends AppCompatActivity {

    // Tempo em milissegundos para exibir texto e transição
    private static final long DELAY_TEXTO = 1000;    // 1 segundo
    private static final long DELAY_TOTAL = 2500;    // 2,5 segundos (tempo total da splash)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Elementos da interface
        ImageView logo = findViewById(R.id.logo);
        TextView textRQM = findViewById(R.id.text_rqm);

        // Animação da logo
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.logo_animation);
        logo.startAnimation(animation);

        // Exibe o texto "RQM" após pequeno atraso
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            textRQM.setVisibility(View.VISIBLE);
            textRQM.setAlpha(0f);
            textRQM.animate().alpha(1f).setDuration(500).start();
        }, DELAY_TEXTO);

        // Após o tempo total da splash, vai direto para o MainActivity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, DELAY_TOTAL);
    }
}
