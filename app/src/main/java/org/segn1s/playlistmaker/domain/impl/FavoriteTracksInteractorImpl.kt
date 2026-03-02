package org.segn1s.playlistmaker.domain.impl

import kotlinx.coroutines.flow.Flow
import org.segn1s.playlistmaker.domain.api.favorites.FavoriteTracksInteractor
import org.segn1s.playlistmaker.domain.api.favorites.FavoriteTracksRepository
import org.segn1s.playlistmaker.domain.model.Track

class FavoriteTracksInteractorImpl(
    private val repository: FavoriteTracksRepository
) : FavoriteTracksInteractor {

    override suspend fun addToFavorites(track: Track) {
        repository.addToFavorites(track)
    }

    override suspend fun removeFromFavorites(track: Track) {
        repository.removeFromFavorites(track)
    }

    override fun getFavoriteTracks(): Flow<List<Track>> {
        // Порядок уже обеспечен запросом в DAO (ORDER BY addedAt DESC)
        return repository.getFavoriteTracks()
    }
}

