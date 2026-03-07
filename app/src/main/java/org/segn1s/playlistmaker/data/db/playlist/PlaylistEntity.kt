package org.segn1s.playlistmaker.data.db.playlist

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,

    val description: String?,

    val coverPath: String?,

    val trackIds: String,

    val tracksCount: Int
)