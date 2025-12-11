package org.segn1s.playlistmaker.presentation

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import org.segn1s.playlistmaker.Creator
import org.segn1s.playlistmaker.domain.api.SettingsInteractor // Импортируем интерфейс Интерактора

class App : Application() {

    // ❗ 1. Интерактор будет использоваться для получения/сохранения настроек
    private lateinit var settingsInteractor: SettingsInteractor

    var darkTheme: Boolean = false
        private set

    // Удаляем поле 'prefs' и 'KEY_DARK_THEME'

    override fun onCreate() {
        super.onCreate()

        // ❗ 2. Инициализация Интерактора через Creator
        // Мы передаем this, чтобы Creator мог получить Context для SharedPreferences.
        settingsInteractor = Creator.provideSettingsInteractor(this)

        // ❗ 3. Получаем сохраненное состояние темы из Domain-слоя
        darkTheme = settingsInteractor.getDarkThemeState()
        applyTheme(darkTheme)
    }

    // ❗ 4. Метод switchTheme теперь отвечает только за вызов Интерактора.
    // Вся логика сохранения SharedPreferences ушла в Domain/Data слои.
    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled

        // Интерактор сохранит состояние и вызовет applyTheme через колбэк
        settingsInteractor.switchTheme(darkThemeEnabled)

        // Примечание: Если applyTheme вызывается внутри switchTheme в Интеракторе, 
        // эту строку можно удалить, чтобы избежать двойного вызова. 
        // Но для ясности пока оставим, если Интерактор сам вызывает App::applyTheme
    }

    // ❗ 5. Применение темы остается, так как это UI-логика.
    fun applyTheme(enabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }

    // Удаляем companion object с KEY_DARK_THEME
}