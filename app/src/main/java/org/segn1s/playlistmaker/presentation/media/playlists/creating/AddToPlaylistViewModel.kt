package org.segn1s.playlistmaker.presentation.media.playlists.creating

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.segn1s.playlistmaker.domain.api.playlist.PlaylistInteractor
import org.segn1s.playlistmaker.domain.model.Playlist
import org.segn1s.playlistmaker.domain.model.Track

sealed class AddToPlaylistResult {
    data class Added(val playlistName: String) : AddToPlaylistResult()
    data class AlreadyExists(val playlistName: String) : AddToPlaylistResult()
}

class AddToPlaylistViewModel(
    private val track: Track,
    private val interactor: PlaylistInteractor
) : ViewModel() {

    private val _playlists = MutableLiveData<List<Playlist>>()
    val playlists: LiveData<List<Playlist>> = _playlists

    private val _addResult = MutableLiveData<AddToPlaylistResult>()
    val addResult: LiveData<AddToPlaylistResult> = _addResult

    fun loadPlaylists() {
        viewModelScope.launch {
            _playlists.postValue(interactor.getPlaylists())
        }
    }

    fun addTrackToPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            val trackIdInt = track.trackId.toInt()
            if (playlist.trackIds.contains(trackIdInt)) {
                _addResult.postValue(AddToPlaylistResult.AlreadyExists(playlist.name))
            } else {
                val newIds: List<Int> = playlist.trackIds + trackIdInt
                val updated = playlist.copy(
                    trackIds = newIds,
                    tracksCount = newIds.size
                )
                interactor.addTrackToStorage(track)
                interactor.updatePlaylist(updated)
                _addResult.postValue(AddToPlaylistResult.Added(playlist.name))
            }
        }
    }
}