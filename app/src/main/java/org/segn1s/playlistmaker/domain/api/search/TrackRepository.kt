package org.segn1s.playlistmaker.domain.api.search

import kotlinx.coroutines.flow.Flow
import org.segn1s.playlistmaker.domain.model.Track

interface TrackRepository {
    // Результат поиска треков
    sealed class SearchResult {
        data class Success(val tracks: List<Track>) : SearchResult()
        data class Error(val errorCode: Int, val message: String?) : SearchResult()
    }

    // Метод для поиска треков возвращает Flow с результатом
    fun searchTracks(expression: String): Flow<SearchResult>
}