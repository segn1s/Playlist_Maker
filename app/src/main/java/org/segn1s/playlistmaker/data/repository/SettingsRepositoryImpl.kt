package org.segn1s.playlistmaker.data.repository

import android.content.SharedPreferences
import org.segn1s.playlistmaker.domain.api.SettingsRepository

private const val DARK_THEME_KEY = "dark_theme_enabled"

class SettingsRepositoryImpl(private val sharedPreferences: SharedPreferences) : SettingsRepository {

    override fun getThemeState(): Boolean {
        // По умолчанию светлая тема (false)
        return sharedPreferences.getBoolean(DARK_THEME_KEY, false)
    }

    override fun updateTheme(isDarkTheme: Boolean) {
        sharedPreferences.edit()
            .putBoolean(DARK_THEME_KEY, isDarkTheme)
            .apply()
    }
}