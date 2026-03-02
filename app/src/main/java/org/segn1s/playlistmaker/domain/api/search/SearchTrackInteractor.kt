package org.segn1s.playlistmaker.domain.api.search

import kotlinx.coroutines.flow.Flow
import org.segn1s.playlistmaker.domain.model.Track

interface SearchTrackInteractor {
    // Результат поиска треков
    sealed class SearchResult {
        data class Success(val tracks: List<Track>) : SearchResult()
        data class Error(val isFailed: Boolean) : SearchResult()
    }

    fun searchTracks(expression: String): Flow<SearchResult>
}