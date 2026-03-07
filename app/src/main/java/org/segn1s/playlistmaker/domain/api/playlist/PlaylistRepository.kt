package org.segn1s.playlistmaker.domain.api.playlist

import android.net.Uri
import org.segn1s.playlistmaker.domain.model.Playlist
import org.segn1s.playlistmaker.domain.model.Track

interface PlaylistRepository {
    suspend fun createPlaylist(playlist: Playlist)
    suspend fun updatePlaylist(playlist: Playlist)
    suspend fun getPlaylists(): List<Playlist>
    fun saveCoverToPrivateStorage(uri: Uri): String

    suspend fun addTrackToStorage(track: Track)
}