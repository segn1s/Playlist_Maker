package org.segn1s.playlistmaker.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.segn1s.playlistmaker.data.ITunesApi
import org.segn1s.playlistmaker.data.db.AppDatabase
import org.segn1s.playlistmaker.data.repository.AudioPlayerRepositoryImpl
import org.segn1s.playlistmaker.data.repository.FavoriteTracksRepositoryImpl
import org.segn1s.playlistmaker.data.repository.PlaylistRepositoryImpl
import org.segn1s.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import org.segn1s.playlistmaker.data.repository.SettingsRepositoryImpl
import org.segn1s.playlistmaker.data.repository.TrackRepositoryImpl
import org.segn1s.playlistmaker.domain.api.favorites.FavoriteTracksInteractor
import org.segn1s.playlistmaker.domain.api.favorites.FavoriteTracksRepository
import org.segn1s.playlistmaker.domain.api.player.AudioPlayerInteractor
import org.segn1s.playlistmaker.domain.api.player.AudioPlayerRepository
import org.segn1s.playlistmaker.domain.api.playlist.PlaylistInteractor
import org.segn1s.playlistmaker.domain.api.playlist.PlaylistRepository
import org.segn1s.playlistmaker.domain.api.search.HistoryInteractor
import org.segn1s.playlistmaker.domain.api.search.SearchHistoryRepository
import org.segn1s.playlistmaker.domain.api.search.SearchTrackInteractor
import org.segn1s.playlistmaker.domain.api.search.TrackRepository
import org.segn1s.playlistmaker.domain.api.settings.SettingsInteractor
import org.segn1s.playlistmaker.domain.api.settings.SettingsRepository
import org.segn1s.playlistmaker.domain.impl.AudioPlayerInteractorImpl
import org.segn1s.playlistmaker.domain.impl.FavoriteTracksInteractorImpl
import org.segn1s.playlistmaker.domain.impl.HistoryInteractorImpl
import org.segn1s.playlistmaker.domain.impl.PlaylistInteractorImpl
import org.segn1s.playlistmaker.domain.impl.SearchTrackInteractorImpl
import org.segn1s.playlistmaker.domain.impl.SettingsInteractorImpl
import org.segn1s.playlistmaker.domain.model.Playlist
import org.segn1s.playlistmaker.domain.model.Track
import org.segn1s.playlistmaker.presentation.App
import org.segn1s.playlistmaker.presentation.media.favs.FavoriteTracksViewModel
import org.segn1s.playlistmaker.presentation.media.MediaViewModel
import org.segn1s.playlistmaker.presentation.playlist_with_tracks.PlaylistWithTracksViewModel
import org.segn1s.playlistmaker.presentation.media.playlists.PlaylistsViewModel
import org.segn1s.playlistmaker.presentation.media.playlists.creating.AddToPlaylistViewModel
import org.segn1s.playlistmaker.presentation.media.playlists.creating.CreatePlaylistViewModel
import org.segn1s.playlistmaker.presentation.player.PlayerViewModel
import org.segn1s.playlistmaker.presentation.playlist_with_tracks.edit_playlist.EditPlaylistViewModel
import org.segn1s.playlistmaker.presentation.search.SearchViewModel
import org.segn1s.playlistmaker.presentation.settings.SettingsViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val dataModule = module {
    // Retrofit & API
    single<ITunesApi> {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ITunesApi::class.java)
    }

    // SharedPreferences & Gson
    single {
        androidContext().getSharedPreferences("playlist_maker_prefs", Context.MODE_PRIVATE)
    }

    factory { Gson() }

    // Room database
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "playlist_maker_db"
        ).fallbackToDestructiveMigration().build()
    }

    single { get<AppDatabase>().playlistDao() }

    single { get<AppDatabase>().favoriteTrackDao() }

    single { get<AppDatabase>().playlistTrackDao() }
}

val repositoryModule = module {
    single<PlaylistRepository> {
        PlaylistRepositoryImpl(get(), get(), androidContext())
    }
    single<TrackRepository> { TrackRepositoryImpl(get(), get()) }
    single<SearchHistoryRepository> { SearchHistoryRepositoryImpl(get(), get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    factory<AudioPlayerRepository> { AudioPlayerRepositoryImpl() }

    single<FavoriteTracksRepository> { FavoriteTracksRepositoryImpl(get()) }
}

val interactorModule = module {
    factory<PlaylistInteractor> { PlaylistInteractorImpl(get()) }
    factory<SearchTrackInteractor> { SearchTrackInteractorImpl(get()) }
    factory<HistoryInteractor> { HistoryInteractorImpl(get(), get()) }
    factory<AudioPlayerInteractor> { AudioPlayerInteractorImpl(get()) }
    factory<FavoriteTracksInteractor> { FavoriteTracksInteractorImpl(get()) }

    // Для SettingsInteractorImpl нужен колбэк applyTheme из App
    factory<SettingsInteractor> {
        val app = androidContext() as App
        SettingsInteractorImpl(get(), app::applyTheme)
    }
}

val viewModelModule = module {
    viewModel { (playlist: Playlist) -> EditPlaylistViewModel(playlist, get()) }
    viewModel { (playlistId: Int) -> PlaylistWithTracksViewModel(playlistId, get()) }
    viewModel { SearchViewModel(get(), get()) }
    viewModel { (track: org.segn1s.playlistmaker.domain.model.Track) ->
        PlayerViewModel(track, get(), get())
    }
    viewModel { SettingsViewModel(get()) }
    viewModel { FavoriteTracksViewModel(get()) }
    viewModel { PlaylistsViewModel(get()) }
    viewModel { CreatePlaylistViewModel(get()) }
    viewModel { (track: Track) -> AddToPlaylistViewModel(track, get()) }
    viewModel { MediaViewModel() }
}