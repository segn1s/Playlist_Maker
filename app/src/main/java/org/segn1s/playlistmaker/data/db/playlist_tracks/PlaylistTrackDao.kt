package org.segn1s.playlistmaker.data.db.playlist_tracks

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PlaylistTrackDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrack(track: PlaylistTrackEntity)

    @Query("SELECT * FROM playlist_tracks WHERE trackId IN (:ids)")
    suspend fun getTracksByIds(ids: List<Int>): List<PlaylistTrackEntity>

    @Query("DELETE FROM playlist_tracks WHERE trackId = :trackId")
    suspend fun deleteTrack(trackId: Int)
}