package org.segn1s.playlistmaker.domain.api

interface SettingsRepository {

    // Получить текущее состояние темной темы
    fun getThemeState(): Boolean

    // Сохранить новое состояние темы
    fun updateTheme(isDarkTheme: Boolean)
}