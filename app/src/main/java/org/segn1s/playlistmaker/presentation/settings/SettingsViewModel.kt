package org.segn1s.playlistmaker.presentation.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.segn1s.playlistmaker.domain.api.settings.SettingsInteractor

class SettingsViewModel(
    private val settingsInteractor: SettingsInteractor
) : ViewModel() {

    // LiveData для состояния свитча
    private val _themeSettingsState = MutableLiveData<Boolean>()
    val themeSettingsState: LiveData<Boolean> = _themeSettingsState

    init {
        // Загружаем начальное состояние при создании
        _themeSettingsState.value = settingsInteractor.getDarkThemeState()
    }

    fun switchTheme(isDark: Boolean) {
        // Чтобы не зациклить обновление, если состояние не изменилось
        if (_themeSettingsState.value == isDark) return

        settingsInteractor.switchTheme(isDark)
        _themeSettingsState.value = isDark
    }
}