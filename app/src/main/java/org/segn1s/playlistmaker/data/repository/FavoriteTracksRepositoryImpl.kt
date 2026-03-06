package org.segn1s.playlistmaker.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.segn1s.playlistmaker.data.db.fav.FavoriteTrackDao
import org.segn1s.playlistmaker.data.db.fav.toDomainTrack
import org.segn1s.playlistmaker.data.db.fav.toFavoriteEntity
import org.segn1s.playlistmaker.domain.api.favorites.FavoriteTracksRepository
import org.segn1s.playlistmaker.domain.model.Track

class FavoriteTracksRepositoryImpl(
    private val favoriteTrackDao: FavoriteTrackDao
) : FavoriteTracksRepository {

    override suspend fun addToFavorites(track: Track) {
        favoriteTrackDao.insert(track.toFavoriteEntity())
    }

    override suspend fun removeFromFavorites(track: Track) {
        favoriteTrackDao.delete(track.toFavoriteEntity())
    }

    override fun getFavoriteTracks(): Flow<List<Track>> {
        return favoriteTrackDao.getAllFavorites()
            .map { entities -> entities.map { it.toDomainTrack() } }
    }
}

