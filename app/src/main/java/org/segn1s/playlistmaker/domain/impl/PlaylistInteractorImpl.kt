package org.segn1s.playlistmaker.domain.impl

import android.net.Uri
import org.segn1s.playlistmaker.domain.api.playlist.PlaylistInteractor
import org.segn1s.playlistmaker.domain.api.playlist.PlaylistRepository
import org.segn1s.playlistmaker.domain.model.Playlist
import org.segn1s.playlistmaker.domain.model.Track

class PlaylistInteractorImpl(
    private val repository: PlaylistRepository
) : PlaylistInteractor {

    override suspend fun createPlaylist(playlist: Playlist) {
        repository.createPlaylist(playlist)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        repository.updatePlaylist(playlist)
    }

    override suspend fun getPlaylists(): List<Playlist> {
        return repository.getPlaylists()
    }

    override fun saveCoverToPrivateStorage(uri: Uri): String {
        return repository.saveCoverToPrivateStorage(uri)
    }

    override suspend fun addTrackToStorage(track: Track) {
        repository.addTrackToStorage(track)
    }
}