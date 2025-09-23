package org.segn1s.playlistmaker

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {

    var darkTheme = false
    private lateinit var prefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences("playlist_maker_prefs", MODE_PRIVATE)
        darkTheme = prefs.getBoolean(KEY_DARK_THEME, false)
        applyTheme(darkTheme)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled
        prefs.edit().putBoolean(KEY_DARK_THEME, darkThemeEnabled).apply()
        applyTheme(darkThemeEnabled)
    }

    private fun applyTheme(enabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }

    companion object {
        private const val KEY_DARK_THEME = "dark_theme"
    }
}