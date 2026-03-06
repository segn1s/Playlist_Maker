package org.segn1s.playlistmaker.presentation.media.playlists.creating

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.segn1s.playlistmaker.domain.api.playlist.PlaylistInteractor
import org.segn1s.playlistmaker.domain.model.Playlist

class CreatePlaylistViewModel(
    private val interactor: PlaylistInteractor
) : ViewModel() {

    fun createPlaylist(name: String, description: String?, coverUri: Uri?) {
        viewModelScope.launch {
            val coverPath = coverUri?.let { interactor.saveCoverToPrivateStorage(it) }
            val playlist = Playlist(
                id = 0,
                name = name,
                description = description?.takeIf { it.isNotBlank() },
                coverPath = coverPath,
                trackIds = emptyList(),
                tracksCount = 0
            )
            interactor.createPlaylist(playlist)
        }
    }
}