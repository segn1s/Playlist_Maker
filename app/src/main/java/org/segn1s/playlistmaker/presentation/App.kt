package org.segn1s.playlistmaker.presentation

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.segn1s.playlistmaker.di.dataModule
import org.segn1s.playlistmaker.di.interactorModule
import org.segn1s.playlistmaker.di.repositoryModule
import org.segn1s.playlistmaker.di.viewModelModule
import org.segn1s.playlistmaker.domain.api.settings.SettingsInteractor

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // Инициализируем Koin
        startKoin {
            androidContext(this@App)
            modules(dataModule, repositoryModule, interactorModule, viewModelModule)
        }

        // Получаем интерактор из Koin для установки начальной темы
        val settingsInteractor: SettingsInteractor = get()
        applyTheme(settingsInteractor.getDarkThemeState())
    }

    fun applyTheme(enabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}