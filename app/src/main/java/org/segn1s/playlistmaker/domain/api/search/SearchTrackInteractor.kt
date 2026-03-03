package org.segn1s.playlistmaker.domain.api.search

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.segn1s.playlistmaker.domain.model.Track
import retrofit2.HttpException
import java.io.IOException

interface SearchTrackInteractor {
    sealed class SearchResult {
        data class Success(val tracks: List<Track>) : SearchResult()
        data class Error(val errorCode: Int, val message: String? = null) : SearchResult()
        data class NetworkError(val exception: IOException) : SearchResult()
        data class ServerError(val code: Int, val message: String?) : SearchResult()
        data class UnknownError(val exception: Throwable) : SearchResult()
    }

    fun searchTracks(expression: String): Flow<SearchResult>
}