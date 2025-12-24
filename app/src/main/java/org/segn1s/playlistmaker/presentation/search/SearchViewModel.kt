package org.segn1s.playlistmaker.presentation.search

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.segn1s.playlistmaker.domain.api.search.HistoryInteractor
import org.segn1s.playlistmaker.domain.api.search.SearchTrackInteractor
import org.segn1s.playlistmaker.domain.model.Track

class SearchViewModel(
    private val searchInteractor: SearchTrackInteractor,
    private val historyInteractor: HistoryInteractor
) : ViewModel() {

    private val handler = Handler(Looper.getMainLooper())
    private var lastQuery: String? = null

    private val _stateLiveData = MutableLiveData<SearchState>()
    val stateLiveData: LiveData<SearchState> = _stateLiveData

    private val searchRunnable = Runnable {
        val query = lastQuery
        if (!query.isNullOrEmpty()) {
            performSearch(query)
        }
    }

    init {
        showHistory()
    }

    fun searchDebounce(changedText: String) {
        // ИСПРАВЛЕНИЕ: Если текст пустой, мы не дебаунсим, а сразу показываем историю.
        // lastQuery обновляем, чтобы логика не ломалась при быстром вводе.
        if (lastQuery == changedText) return
        this.lastQuery = changedText

        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY_MILLIS)
    }

    fun performSearch(query: String) {
        if (query.isEmpty()) {
            showHistory()
            return
        }

        // Обновляем lastQuery на случай, если поиск вызван кнопкой "Обновить"
        lastQuery = query

        renderState(SearchState.Loading)

        searchInteractor.searchTracks(query, object : SearchTrackInteractor.TracksConsumer {
            override fun consume(foundTracks: List<Track>, isFailed: Boolean) {
                if (isFailed) {
                    renderState(SearchState.ErrorNetwork)
                } else if (foundTracks.isEmpty()) {
                    renderState(SearchState.ErrorNotFound)
                } else {
                    renderState(SearchState.Content(foundTracks))
                }
            }
        })
    }

    fun showHistory() {
        // ИСПРАВЛЕНИЕ: При переходе к истории сбрасываем lastQuery,
        // чтобы следующий поиск (даже такой же) сработал корректно.
        lastQuery = null

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

    override fun onCleared() {
        handler.removeCallbacks(searchRunnable)
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY_MILLIS = 2000L
    }
}