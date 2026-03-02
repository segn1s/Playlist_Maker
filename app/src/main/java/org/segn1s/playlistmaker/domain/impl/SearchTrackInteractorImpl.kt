package org.segn1s.playlistmaker.domain.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.segn1s.playlistmaker.domain.api.search.SearchTrackInteractor
import org.segn1s.playlistmaker.domain.api.search.TrackRepository

class SearchTrackInteractorImpl(private val repository: TrackRepository) : SearchTrackInteractor {

    override fun searchTracks(expression: String): Flow<SearchTrackInteractor.SearchResult> {
        return repository.searchTracks(expression).map { repositoryResult ->
            when (repositoryResult) {
                is TrackRepository.SearchResult.Success -> {
                    // Если нужно отфильтровать или отсортировать результаты, это делается здесь
                    SearchTrackInteractor.SearchResult.Success(repositoryResult.tracks)
                }
                is TrackRepository.SearchResult.Error -> {
                    // Здесь может быть бизнес-логика обработки кодов ошибок
                    SearchTrackInteractor.SearchResult.Error(true)
                }
            }
        }
    }
}