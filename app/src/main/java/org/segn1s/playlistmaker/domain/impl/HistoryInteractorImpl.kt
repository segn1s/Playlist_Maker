package org.segn1s.playlistmaker.domain.impl

import org.segn1s.playlistmaker.data.db.fav.FavoriteTrackDao
import org.segn1s.playlistmaker.domain.api.search.HistoryInteractor
import org.segn1s.playlistmaker.domain.api.search.SearchHistoryRepository
import org.segn1s.playlistmaker.domain.model.Track

class HistoryInteractorImpl(
    private val repository: SearchHistoryRepository,
    private val favoriteTrackDao: FavoriteTrackDao
) : HistoryInteractor {

    override fun addTrack(track: Track) {
        repository.addTrack(track)
    }

    override fun getHistory(): List<Track> {
        val history = repository.getHistory()
        val favoriteIds = runCatching { favoriteTrackDao.getAllFavoriteIds().toSet() }
            .getOrDefault(emptySet())

        return history.onEach { track ->
            track.isFavorite = track.trackId in favoriteIds
        }
    }

    override fun clearHistory() {
        repository.clearHistory()
    }
}