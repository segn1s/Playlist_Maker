package org.segn1s.playlistmaker.domain.api.playlist

import android.net.Uri
import org.segn1s.playlistmaker.domain.model.Playlist
import org.segn1s.playlistmaker.domain.model.Track

class PlaylistInteractor(
    private val repository: PlaylistRepository
) {
    suspend fun createPlaylist(playlist: Playlist) {
        repository.createPlaylist(playlist)
    }

    suspend fun updatePlaylist(playlist: Playlist) {
        repository.updatePlaylist(playlist)
    }

    suspend fun getPlaylists(): List<Playlist> {
        return repository.getPlaylists()
    }

    fun saveCoverToPrivateStorage(uri: Uri): String {
        return repository.saveCoverToPrivateStorage(uri)
    }

    suspend fun addTrackToStorage(track: Track) {
        repository.addTrackToStorage(track)
    }
}