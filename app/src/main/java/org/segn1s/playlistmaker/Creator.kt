package org.segn1s.playlistmaker

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.gson.Gson
import org.segn1s.playlistmaker.data.ITunesApi
import org.segn1s.playlistmaker.data.repository.AudioPlayerRepositoryImpl
import org.segn1s.playlistmaker.domain.impl.HistoryInteractorImpl
import org.segn1s.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import org.segn1s.playlistmaker.data.repository.SettingsRepositoryImpl
import org.segn1s.playlistmaker.data.repository.TrackRepositoryImpl
import org.segn1s.playlistmaker.domain.api.player.AudioPlayerInteractor
import org.segn1s.playlistmaker.domain.api.player.AudioPlayerRepository
import org.segn1s.playlistmaker.domain.api.search.HistoryInteractor
import org.segn1s.playlistmaker.domain.api.search.SearchHistoryRepository
import org.segn1s.playlistmaker.domain.api.search.SearchTrackInteractor
import org.segn1s.playlistmaker.domain.api.settings.SettingsInteractor
import org.segn1s.playlistmaker.domain.api.settings.SettingsRepository
import org.segn1s.playlistmaker.domain.api.search.TrackRepository
import org.segn1s.playlistmaker.domain.impl.AudioPlayerInteractorImpl
import org.segn1s.playlistmaker.domain.impl.SearchTrackInteractorImpl
import org.segn1s.playlistmaker.domain.impl.SettingsInteractorImpl
import org.segn1s.playlistmaker.presentation.App
import org.segn1s.playlistmaker.presentation.settings.SettingsViewModel
import org.segn1s.playlistmaker.presentation.player.PlayerViewModel
import org.segn1s.playlistmaker.presentation.search.SearchViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Creator {

    private fun getTrackRepository(): TrackRepository {
        return TrackRepositoryImpl(getITunesApi())
    }

    fun provideSearchTrackInteractor(): SearchTrackInteractor {
        return SearchTrackInteractorImpl(getTrackRepository())
    }

    // Вспомогательный метод для Retrofit (может быть вынесен в data.network)
    private fun getITunesApi(): ITunesApi {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ITunesApi::class.java)
    }

    private const val PLAYLIST_MAKER_PREFS = "playlist_maker_prefs"

    private fun getSettingsRepository(context: Context): SettingsRepository {
        // SharedPreferences инициализируется здесь, в Data-слое
        val sharedPrefs = context.getSharedPreferences(PLAYLIST_MAKER_PREFS, MODE_PRIVATE)
        return SettingsRepositoryImpl(sharedPrefs)
    }

    // ❗ ИСПРАВЛЕНИЕ ОШИБКИ: Теперь Creator принимает Context и использует его для создания App
    fun provideSettingsInteractor(applicationContext: Context): SettingsInteractor {

        // Получаем экземпляр App из Context
        val app = applicationContext as App

        // Передаем функцию applyTheme в качестве themeSwitcher (колбэка)
        return SettingsInteractorImpl(
            repository = getSettingsRepository(applicationContext),
            themeSwitcher = app::applyTheme // <-- Идеальная передача колбэка в Kotlin
        )
    }

    private fun getHistoryRepository(context: Context): SearchHistoryRepository {
        val sharedPrefs = context.getSharedPreferences(PLAYLIST_MAKER_PREFS, MODE_PRIVATE)
        // Передаем SharedPreferences и Gson в реализацию репозитория
        return SearchHistoryRepositoryImpl(sharedPrefs, Gson())
    }

    fun provideHistoryInteractor(context: Context): HistoryInteractor {
        return HistoryInteractorImpl(getHistoryRepository(context))
    }

    private fun getAudioPlayerRepository(): AudioPlayerRepository {
        return AudioPlayerRepositoryImpl()
    }

    fun provideAudioPlayerInteractor(): AudioPlayerInteractor {
        return AudioPlayerInteractorImpl(getAudioPlayerRepository())
    }

    fun getSearchViewModelFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            SearchViewModel(
                searchInteractor = provideSearchTrackInteractor(),
                historyInteractor = provideHistoryInteractor(context.applicationContext)
            )
        }
    }

    fun getPlayerViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            PlayerViewModel(provideAudioPlayerInteractor())
        }
    }

    fun getSettingsViewModelFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            SettingsViewModel(
                settingsInteractor = provideSettingsInteractor(context.applicationContext)
            )
        }
    }
}