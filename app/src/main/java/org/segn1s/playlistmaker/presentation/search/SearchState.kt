package org.segn1s.playlistmaker.presentation.search

import org.segn1s.playlistmaker.domain.model.Track

sealed interface SearchState {
    object Loading : SearchState
    data class Content(val tracks: List<Track>) : SearchState
    object ErrorNetwork : SearchState
    object ErrorNotFound : SearchState
    data class History(val tracks: List<Track>) : SearchState
    object Empty : SearchState
}