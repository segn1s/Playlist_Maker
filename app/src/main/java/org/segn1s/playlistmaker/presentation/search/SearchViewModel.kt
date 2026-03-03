package org.segn1s.playlistmaker.presentation.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.segn1s.playlistmaker.domain.api.search.HistoryInteractor
import org.segn1s.playlistmaker.domain.api.search.SearchTrackInteractor
import org.segn1s.playlistmaker.domain.model.Track
import org.segn1s.playlistmaker.presentation.search.SearchState.*

@OptIn(FlowPreview::class)
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

                    if (query.isBlank()) {
                        showHistory()
                        return@collectLatest
                    }

                    renderState(SearchState.Loading)

                    searchInteractor
                        .searchTracks(query)
                        .collectLatest { result ->
                            when (result) {
                                is SearchTrackInteractor.SearchResult.Success -> {
                                    if (result.tracks.isEmpty()) {
                                        renderState(SearchState.ErrorNotFound)
                                    } else {
                                        renderState(Content(result.tracks))
                                    }
                                }

                                is SearchTrackInteractor.SearchResult.Error -> {
                                    // Логируем и показываем сообщение
                                    Log.e("SEARCH", "Error code = ${result.errorCode}, message = ${result.message}")
                                    renderState(SearchState.ErrorNetwork) // или можно создать SearchState.ErrorMessage(result.message)
                                }

                                is SearchTrackInteractor.SearchResult.NetworkError -> {
                                    Log.e("SEARCH", "Network error: ${result.exception}")
                                    renderState(SearchState.ErrorNetwork)
                                }

                                is SearchTrackInteractor.SearchResult.UnknownError -> {
                                    Log.e("SEARCH", "Unknown error: ${result.exception}")
                                    renderState(SearchState.ErrorNetwork)
                                }

                                is SearchTrackInteractor.SearchResult.ServerError -> {
                                    Log.e("SEARCH", "Unknown error: ${result}")
                                    renderState(SearchState.ErrorNetwork)
                                }
                            }
                        }
                }
        }
    }

    fun searchDebounce(text: String) {
        searchQueryFlow.value = text
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