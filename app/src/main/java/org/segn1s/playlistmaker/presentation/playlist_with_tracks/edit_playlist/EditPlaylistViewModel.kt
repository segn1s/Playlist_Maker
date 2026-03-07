package org.segn1s.playlistmaker.presentation.playlist_with_tracks.edit_playlist

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import org.segn1s.playlistmaker.domain.api.playlist.PlaylistInteractor
import org.segn1s.playlistmaker.domain.model.Playlist
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.segn1s.playlistmaker.presentation.media.playlists.creating.CreatePlaylistViewModel

class EditPlaylistViewModel(
    private val playlist: Playlist,
    interactor: PlaylistInteractor
) : CreatePlaylistViewModel(interactor) {

    val initialPlaylist = MutableLiveData<Playlist>().apply { value = playlist }

    override fun createPlaylist(name: String, description: String?, coverUri: Uri?) {
        viewModelScope.launch {
            val coverPath = if (coverUri != null && !coverUri.toString().startsWith("/")) {
                interactor.saveCoverToPrivateStorage(coverUri)
            } else {
                coverUri?.toString() ?: playlist.coverPath
            }
            val updated = playlist.copy(
                name = name,
                description = description?.takeIf { it.isNotBlank() },
                coverPath = coverPath
            )
            interactor.updatePlaylist(updated)
        }
    }
}