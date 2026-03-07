package org.segn1s.playlistmaker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import org.segn1s.playlistmaker.data.db.fav.FavoriteTrackDao
import org.segn1s.playlistmaker.data.db.fav.FavoriteTrackEntity
import org.segn1s.playlistmaker.data.db.playlist.PlaylistDao
import org.segn1s.playlistmaker.data.db.playlist.PlaylistEntity
import org.segn1s.playlistmaker.data.db.playlist_tracks.PlaylistTrackDao
import org.segn1s.playlistmaker.data.db.playlist_tracks.PlaylistTrackEntity

@Database(
    entities = [FavoriteTrackEntity::class, PlaylistEntity::class, PlaylistTrackEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteTrackDao(): FavoriteTrackDao
    abstract fun playlistDao(): PlaylistDao

    abstract fun playlistTrackDao(): PlaylistTrackDao
}