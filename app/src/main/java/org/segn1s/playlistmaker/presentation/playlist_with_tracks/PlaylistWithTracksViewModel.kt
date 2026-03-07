package org.segn1s.playlistmaker.presentation.playlist_with_tracks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.segn1s.playlistmaker.domain.api.playlist.PlaylistInteractor
import org.segn1s.playlistmaker.domain.model.Playlist
import org.segn1s.playlistmaker.domain.model.Track

class PlaylistWithTracksViewModel(
    private val playlistId: Int,
    private val interactor: PlaylistInteractor
) : ViewModel() {

    private val _playlist = MutableLiveData<Playlist>()
    val playlist: LiveData<Playlist> = _playlist

    private val _tracks = MutableLiveData<List<Track>>()
    val tracks: LiveData<List<Track>> = _tracks

    private val _totalMinutes = MutableLiveData<Long>()
    val totalMinutes: LiveData<Long> = _totalMinutes

    fun loadPlaylist() {
        viewModelScope.launch {
            val playlist = interactor.getPlaylistById(playlistId)
            _playlist.postValue(playlist)

            val tracks = interactor.getTracksByIds(playlist.trackIds)
            _tracks.postValue(tracks.reversed())

            val totalMillis = tracks.sumOf { it.trackTimeMillis ?: 0L }
            _totalMinutes.postValue(totalMillis / 1000 / 60)
        }
    }

    fun deleteTrackFromPlaylist(track: Track) {
        viewModelScope.launch {
            val current = _playlist.value ?: return@launch
            val newIds = current.trackIds - track.trackId.toInt()
            val updated = current.copy(
                trackIds = newIds,
                tracksCount = newIds.size
            )
            interactor.updatePlaylist(updated)
            interactor.deleteTrackFromPlaylist(track.trackId.toInt())
            loadPlaylist()
        }
    }

    fun deletePlaylist(onDeleted: () -> Unit) {
        viewModelScope.launch {
            interactor.deletePlaylist(playlistId)
            onDeleted()
        }
    }
}