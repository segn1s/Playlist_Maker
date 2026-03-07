package org.segn1s.playlistmaker.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import org.segn1s.playlistmaker.data.db.playlist.PlaylistDao
import org.segn1s.playlistmaker.data.db.playlist.PlaylistEntity
import org.segn1s.playlistmaker.data.db.playlist_tracks.PlaylistTrackDao
import org.segn1s.playlistmaker.data.db.playlist_tracks.PlaylistTrackEntity
import org.segn1s.playlistmaker.domain.api.playlist.PlaylistRepository
import org.segn1s.playlistmaker.domain.model.Playlist
import org.segn1s.playlistmaker.domain.model.Track
import java.io.File
import java.io.FileOutputStream

class PlaylistRepositoryImpl(
    private val dao: PlaylistDao,
    private val trackDao: PlaylistTrackDao,
    private val context: Context
) : PlaylistRepository {

    override suspend fun createPlaylist(playlist: Playlist) {
        dao.insertPlaylist(playlist.toEntity())
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        dao.updatePlaylist(playlist.toEntity())
    }

    override suspend fun getPlaylists(): List<Playlist> {
        return dao.getPlaylists().map { it.toDomain() }
    }

    override suspend fun addTrackToStorage(track: Track) {
        trackDao.insertTrack(track.toTrackEntity())
    }

    override fun saveCoverToPrivateStorage(uri: Uri): String {
        val dir = File(context.filesDir, "covers")
        if (!dir.exists()) dir.mkdirs()

        val fileName = "cover_${System.currentTimeMillis()}.jpg"
        val file = File(dir, fileName)

        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }

        return file.absolutePath
    }

    private fun PlaylistEntity.toDomain() = Playlist(
        id = id,
        name = name,
        description = description,
        coverPath = coverPath,
        trackIds = if (trackIds.isBlank()) emptyList()
        else trackIds.split(",").map { it.trim().toInt() },
        tracksCount = tracksCount
    )

    private fun Playlist.toEntity() = PlaylistEntity(
        id = id,
        name = name,
        description = description,
        coverPath = coverPath,
        trackIds = trackIds.joinToString(","),
        tracksCount = tracksCount
    )

    private fun Track.toTrackEntity() = PlaylistTrackEntity(
        trackId = trackId.toInt(),
        trackName = trackName,
        artistName = artistName,
        trackTime = trackTime ?: "",
        artworkUrl100 = artworkUrl100,
        collectionName = collectionName,
        releaseDate = releaseDate,
        primaryGenreName = primaryGenreName,
        country = country,
        previewUrl = previewUrl
    )
}