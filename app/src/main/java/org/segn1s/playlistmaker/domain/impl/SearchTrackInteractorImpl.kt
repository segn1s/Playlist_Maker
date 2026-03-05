package org.segn1s.playlistmaker.domain.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.segn1s.playlistmaker.domain.api.search.SearchTrackInteractor
import org.segn1s.playlistmaker.domain.api.search.TrackRepository
import retrofit2.HttpException
import java.io.IOException

class SearchTrackInteractorImpl(
    private val repository: TrackRepository
) : SearchTrackInteractor {

    override fun searchTracks(expression: String): Flow<SearchTrackInteractor.SearchResult> {
        return repository.searchTracks(expression)
            .map { result ->
                when (result) {
                    is TrackRepository.SearchResult.Success -> {
                        SearchTrackInteractor.SearchResult.Success(result.tracks)
                    }

                    is TrackRepository.SearchResult.Error -> {
                        SearchTrackInteractor.SearchResult.ServerError(
                            code = result.errorCode,
                            message = result.message
                        )
                    }
                }
            }
            .catch { e ->
                when (e) {
                    is IOException -> {
                        emit(SearchTrackInteractor.SearchResult.NetworkError(e))
                    }

                    is HttpException -> {
                        emit(
                            SearchTrackInteractor.SearchResult.ServerError(
                                code = e.code(),
                                message = e.message()
                            )
                        )
                    }

                    else -> {
                        emit(SearchTrackInteractor.SearchResult.UnknownError(e))
                    }
                }
            }
    }
}