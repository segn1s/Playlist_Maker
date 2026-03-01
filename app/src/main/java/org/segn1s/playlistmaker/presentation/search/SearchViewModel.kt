package org.segn1s.playlistmaker.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.segn1s.playlistmaker.domain.api.search.HistoryInteractor
import org.segn1s.playlistmaker.domain.api.search.SearchTrackInteractor
import org.segn1s.playlistmaker.domain.model.Track

class SearchViewModel(
    private val searchInteractor: SearchTrackInteractor,
    private val historyInteractor: HistoryInteractor
) : ViewModel() {

    private val _stateLiveData = MutableLiveData<SearchState>()
    val stateLiveData: LiveData<SearchState> = _stateLiveData

    private val searchQueryFlow = MutableStateFlow("")

    init {
        showHistory()

        viewModelScope.launch {
            searchQueryFlow
                .debounce(SEARCH_DEBOUNCE_DELAY_MILLIS)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isEmpty()) {
                        showHistory()
                    } else {
                        performSearch(query)
                    }
                }
        }
    }

    fun searchDebounce(changedText: String) {
        searchQueryFlow.value = changedText
    }

    fun performSearch(query: String) {
        if (query.isEmpty()) {
            showHistory()
            return
        }

        renderState(SearchState.Loading)

        viewModelScope.launch {
            searchInteractor.searchTracks(query).collect { result ->
                when (result) {
                    is SearchTrackInteractor.SearchResult.Success -> {
                        if (result.tracks.isEmpty()) {
                            renderState(SearchState.ErrorNotFound)
                        } else {
                            renderState(SearchState.Content(result.tracks))
                        }
                    }
                    is SearchTrackInteractor.SearchResult.Error -> {
                        renderState(SearchState.ErrorNetwork)
                    }
                }
            }
        }
    }

    fun showHistory() {
        val history = historyInteractor.getHistory()
        if (history.isNotEmpty()) {
            renderState(SearchState.History(history))
        } else {
            renderState(SearchState.Empty)
        }
    }

    fun clearHistory() {
        historyInteractor.clearHistory()
        renderState(SearchState.Empty)
    }

    fun addTrackToHistory(track: Track) {
        historyInteractor.addTrack(track)
    }

    private fun renderState(state: SearchState) {
        _stateLiveData.postValue(state)
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY_MILLIS = 2000L
    }
}