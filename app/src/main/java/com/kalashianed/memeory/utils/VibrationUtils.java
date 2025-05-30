package com.kalashianed.memeory.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

/**
 * Утилитный класс для работы с вибрацией
 */
public class VibrationUtils {
    private static final String TAG = "VibrationUtils";
    private static final String PREFS_FILE = "memeory_prefs";
    private static final String PREF_VIBRATION = "vibration_enabled";

    /**
     * Проверяет, включена ли вибрация в настройках приложения
     * 
     * @param context Контекст приложения
     * @return true, если вибрация включена
     */
    public static boolean isVibrationEnabled(Context context) {
        if (context == null) return false;
        
        SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        return prefs.getBoolean(PREF_VIBRATION, true);
    }

    /**
     * Вибрирует короткое время (если вибрация включена в настройках)
     * 
     * @param context Контекст приложения
     */
    public static void vibrateShort(Context context) {
        vibrate(context, 50);
    }

    /**
     * Вибрирует среднее время (если вибрация включена в настройках)
     * 
     * @param context Контекст приложения
     */
    public static void vibrateMedium(Context context) {
        vibrate(context, 100);
    }

    /**
     * Вибрирует долгое время (если вибрация включена в настройках)
     * 
     * @param context Контекст приложения
     */
    public static void vibrateLong(Context context) {
        vibrate(context, 200);
    }

    /**
     * Выполняет вибрацию с указанной длительностью
     * 
     * @param context Контекст приложения
     * @param duration Длительность вибрации в миллисекундах
     */
    public static void vibrate(Context context, long duration) {
        if (context == null) return;
        
        // Проверяем, включена ли вибрация в настройках
        if (!isVibrationEnabled(context)) {
            Log.d(TAG, "Вибрация отключена в настройках");
            return;
        }
        
        try {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            
            if (vibrator == null || !vibrator.hasVibrator()) {
                Log.d(TAG, "Устройство не поддерживает вибрацию");
                return;
            }
            
            // Используем новый API для вибрации на Android 8.0 и выше
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                // Устаревший метод для более ранних версий Android
                vibrator.vibrate(duration);
            }
            
            Log.d(TAG, "Вибрация выполнена: " + duration + " мс");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при выполнении вибрации: " + e.getMessage(), e);
        }
    }
} 