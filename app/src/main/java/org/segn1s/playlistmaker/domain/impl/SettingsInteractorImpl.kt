package org.segn1s.playlistmaker.domain.impl

import org.segn1s.playlistmaker.domain.api.settings.SettingsInteractor
import org.segn1s.playlistmaker.domain.api.settings.SettingsRepository

// ❗ ВАЖНО: Добавляем функциональный тип (колбэк) для Application
// Это нужно, чтобы интерактор мог вызвать изменение темы на уровне Application,
// не зная при этом, как именно Application это делает (инверсия зависимостей).
class SettingsInteractorImpl(
    private val repository: SettingsRepository,
    private val themeSwitcher: (Boolean) -> Unit // Функция, которая изменит тему в App.kt
) : SettingsInteractor {

    override fun getDarkThemeState(): Boolean {
        return repository.getThemeState()
    }

    override fun switchTheme(isDarkTheme: Boolean) {
        // 1. Сначала сохраняем состояние в Data-слое (SharedPreferences)
        repository.updateTheme(isDarkTheme)

        // 2. Затем вызываем функцию для фактического изменения темы в App
        themeSwitcher.invoke(isDarkTheme)
    }
}