package org.segn1s.playlistmaker.data.repository

import org.segn1s.playlistmaker.domain.api.HistoryInteractor
import org.segn1s.playlistmaker.domain.api.SearchHistoryRepository
import org.segn1s.playlistmaker.domain.model.Track

class HistoryInteractorImpl(
    private val repository: SearchHistoryRepository
) : HistoryInteractor {

    override fun addTrack(track: Track) {
        repository.addTrack(track)
    }

    override fun getHistory(): List<Track> {
        // Здесь могла бы быть дополнительная логика (например, аудит или фильтрация),
        // но пока просто делегируем вызов репозиторию.
        return repository.getHistory()
    }

    override fun clearHistory() {
        repository.clearHistory()
    }
}