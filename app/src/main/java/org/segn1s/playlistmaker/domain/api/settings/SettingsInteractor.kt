package org.segn1s.playlistmaker.domain.api.settings

interface SettingsInteractor {

    // Получить состояние темы (для инициализации UI)
    fun getDarkThemeState(): Boolean

    // Переключить тему.
    // Поскольку переключение темы — это еще и изменение UI приложения,
    // мы добавим дополнительный параметр для Application.
    fun switchTheme(isDarkTheme: Boolean)
}