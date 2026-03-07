package org.segn1s.playlistmaker.domain.api.playlist

import android.net.Uri
import org.segn1s.playlistmaker.domain.model.Playlist
import org.segn1s.playlistmaker.domain.model.Track

interface PlaylistRepository {

    suspend fun createPlaylist(playlist: Playlist)

    suspend fun updatePlaylist(playlist: Playlist)

    suspend fun getPlaylists(): List<Playlist>

    suspend fun getPlaylistById(id: Int): Playlist

    suspend fun deletePlaylist(id: Int)

    suspend fun getTracksByIds(ids: List<Int>): List<Track>

    suspend fun deleteTrackFromPlaylist(trackId: Int)

    suspend fun addTrackToStorage(track: Track)

    fun saveCoverToPrivateStorage(uri: Uri): String
}