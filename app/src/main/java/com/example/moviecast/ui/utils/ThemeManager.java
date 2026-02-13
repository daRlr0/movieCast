package com.example.moviecast.ui.utils;

import android.content.Context;
import android.content.SharedPreferences; // Для хранения настроек темы

import androidx.appcompat.app.AppCompatDelegate; // Для управления темой приложения

/**
 * ThemeManager - переключение светлой/тёмной темы.
 * SharedPreferences сохраняет выбор, AppCompatDelegate применяет тему.
 */
public class ThemeManager {
    
    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME_MODE = "theme_mode";
    
    public static final int MODE_LIGHT = AppCompatDelegate.MODE_NIGHT_NO;
    public static final int MODE_DARK = AppCompatDelegate.MODE_NIGHT_YES;
    public static final int MODE_SYSTEM = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    
    // Вызывать в onCreate() каждой Activity до setContentView()
    public static void applyTheme(Context context) {
        int themeMode = getSavedThemeMode(context);
        // Применяем тему через AppCompatDelegate
        AppCompatDelegate.setDefaultNightMode(themeMode);
    }
    
    // Кнопка в Toolbar - переключает тему и сохраняет в SharedPreferences
    public static void toggleTheme(Context context) {
        int currentMode = getSavedThemeMode(context);
        
        // Переключение между светлой и темной темой (системный режим пропускаем)
        int newMode;
        if (currentMode == MODE_DARK) {
            newMode = MODE_LIGHT; // Темная -> Светлая
        } else {
            newMode = MODE_DARK;  // Светлая -> Темная
        }
        
        // Сохраняем в SharedPreferences
        saveThemeMode(context, newMode);
        // Применяем новую тему мгновенно
        AppCompatDelegate.setDefaultNightMode(newMode);
    }
    
    public static void setThemeMode(Context context, int mode) {
        saveThemeMode(context, mode);
        AppCompatDelegate.setDefaultNightMode(mode);
    }
    
    public static int getSavedThemeMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // По умолчанию используем системную тему
        return prefs.getInt(KEY_THEME_MODE, MODE_SYSTEM);
    }
    
    private static void saveThemeMode(Context context, int mode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply();
    }
    
    // Для иконки в меню (солнце/луна)
    public static boolean isDarkMode(Context context) {
        int mode = getSavedThemeMode(context);
        if (mode == MODE_DARK) {
            return true; // Принудительно темная тема
        } else if (mode == MODE_LIGHT) {
            return false; // Принудительно светлая тема
        } else {
            // Режим MODE_SYSTEM - проверяем системную настройку
            int nightMode = context.getResources().getConfiguration().uiMode 
                    & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            return nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        }
    }
}
