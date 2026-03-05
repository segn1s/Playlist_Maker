package org.segn1s.playlistmaker.domain.api.favorites

import kotlinx.coroutines.flow.Flow
import org.segn1s.playlistmaker.domain.model.Track

interface FavoriteTracksRepository {
    suspend fun addToFavorites(track: Track)
    suspend fun removeFromFavorites(track: Track)
    fun getFavoriteTracks(): Flow<List<Track>>
}

