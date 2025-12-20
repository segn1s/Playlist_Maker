package org.segn1s.playlistmaker.domain.api.settings

interface SettingsRepository {

    // Получить текущее состояние темной темы
    fun getThemeState(): Boolean

    // Сохранить новое состояние темы
    fun updateTheme(isDarkTheme: Boolean)
}