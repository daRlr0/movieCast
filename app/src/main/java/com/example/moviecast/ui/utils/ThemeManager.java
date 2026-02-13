package com.example.moviecast.ui.utils;

import android.content.Context;
import android.content.SharedPreferences; // Для хранения настроек темы

import androidx.appcompat.app.AppCompatDelegate; // Для управления темой приложения

/**
 * ThemeManager - утилита для управления темой приложения (светлая/темная)
 * 
 * Назначение:
 * Этот класс управляет переключением между светлой и темной темой приложения.
 * Использует SharedPreferences для сохранения выбора пользователя.
 * Использует AppCompatDelegate для применения темы ко всему приложению.
 * 
 * Особенности:
 * - Сохранение выбора пользователя между запусками приложения
 * - Поддержка трех режимов: светлый, темный, системный
 * - Мгновенное применение темы без перезапуска приложения
 */
public class ThemeManager {
    
    // Имя файла SharedPreferences для хранения настроек темы
    private static final String PREFS_NAME = "theme_prefs";
    // Ключ для хранения режима темы
    private static final String KEY_THEME_MODE = "theme_mode";
    
    // Константы режимов темы
    public static final int MODE_LIGHT = AppCompatDelegate.MODE_NIGHT_NO;        // Светлая тема
    public static final int MODE_DARK = AppCompatDelegate.MODE_NIGHT_YES;        // Темная тема
    public static final int MODE_SYSTEM = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM; // Следовать системной теме
    
    /**
     * Применение сохраненной темы из SharedPreferences
     * Вызывается в onCreate() каждой Activity перед setContentView()
     * 
     * @param context - контекст приложения
     */
    public static void applyTheme(Context context) {
        int themeMode = getSavedThemeMode(context);
        // Применяем тему через AppCompatDelegate
        AppCompatDelegate.setDefaultNightMode(themeMode);
    }
    
    /**
     * Переключение между светлой и темной темой
     * Используется при нажатии на кнопку переключения темы в Toolbar
     * Автоматически сохраняет выбор в SharedPreferences
     * 
     * @param context - контекст приложения
     */
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
    
    /**
     * Установка конкретного режима темы
     * 
     * @param context - контекст приложения
     * @param mode - режим темы (MODE_LIGHT, MODE_DARK или MODE_SYSTEM)
     */
    public static void setThemeMode(Context context, int mode) {
        saveThemeMode(context, mode);
        AppCompatDelegate.setDefaultNightMode(mode);
    }
    
    /**
     * Получение сохраненного режима темы из SharedPreferences
     * 
     * @param context - контекст приложения
     * @return режим темы (MODE_LIGHT, MODE_DARK или MODE_SYSTEM)
     */
    public static int getSavedThemeMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // По умолчанию используем системную тему
        return prefs.getInt(KEY_THEME_MODE, MODE_SYSTEM);
    }
    
    /**
     * Сохранение режима темы в SharedPreferences
     * Используется для сохранения выбора пользователя
     * 
     * @param context - контекст приложения
     * @param mode - режим темы для сохранения
     */
    private static void saveThemeMode(Context context, int mode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply();
    }
    
    /**
     * Проверка, включена ли сейчас темная тема
     * Используется для обновления иконки переключателя темы (солнце/луна)
     * 
     * @param context - контекст приложения
     * @return true если темная тема активна, false если светлая
     */
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
