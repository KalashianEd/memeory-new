package com.kalashianed.memeory;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.kalashianed.memeory.fragments.HomeFragment;
import com.kalashianed.memeory.game.GameManager;

/**
 * Активность для отображения результатов игры
 */
public class ResultsActivity extends AppCompatActivity {

    private static final String TAG = "ResultsActivity";
    public static final String EXTRA_SCORE = "extra_score";
    public static final String EXTRA_TOTAL = "extra_total";
    
    private int percentage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        
        // Получение результатов из Intent
        int score = getIntent().getIntExtra(EXTRA_SCORE, 0);
        int total = getIntent().getIntExtra(EXTRA_TOTAL, 0);

        // Расчет процента правильных ответов
        percentage = total > 0 ? (score * 100) / total : 0;

        // Инициализация представлений
        TextView tvScore = findViewById(R.id.tvScore);
        TextView tvDetails = findViewById(R.id.tvScoreDetails);
        Button btnPlayAgain = findViewById(R.id.btnPlayAgain);
        Button btnHome = findViewById(R.id.btnHome);
        Button btnShare = findViewById(R.id.btnShare);

        // Установка текста с результатами
        tvScore.setText(percentage + "%");
        tvDetails.setText(getString(R.string.score_details, score, total));
        
        // Обновляем локальные данные
        updateLocalScore(percentage);

        // Обработчик кнопки "Играть снова"
        btnPlayAgain.setOnClickListener(v -> {
            // Сбрасываем флаг, чтобы начать новую игру
            HomeFragment.resetGameState();
            
            // Возвращаемся на главный экран
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        
        // Обработчик кнопки "На главный экран"
        btnHome.setOnClickListener(v -> {
            // Сбрасываем флаг игры
            HomeFragment.resetGameState();
            
            // Возвращаемся на главный экран без запуска новой игры
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Обработчик кнопки "Поделиться"
        btnShare.setOnClickListener(v -> {
            String shareMessage = getString(R.string.share_message, percentage);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Поделиться через"));
        });
    }
    
    /**
     * Обновляет локальную статистику пользователя
     */
    private void updateLocalScore(int scorePercentage) {
        SharedPreferences prefs = getSharedPreferences(GameManager.PREF_NAME, Context.MODE_PRIVATE);
        int currentBestScore = prefs.getInt(GameManager.KEY_BEST_SCORE, 0);
        
        // Обновляем только если текущий счет лучше предыдущего
        if (scorePercentage > currentBestScore) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(GameManager.KEY_BEST_SCORE, scorePercentage);
            editor.apply();
            
            Log.d(TAG, "Обновлён локальный рекорд: " + scorePercentage + "%");
        }
    }
} 