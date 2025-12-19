package org.segn1s.playlistmaker.domain.api

import org.segn1s.playlistmaker.domain.model.Track

interface HistoryInteractor {

    // Добавить трек в историю (вызывается из Activity)
    fun addTrack(track: Track)

    // Получить историю для отображения (вызывается из Activity)
    fun getHistory(): List<Track>

    // Очистить историю (вызывается из Activity)
    fun clearHistory()
}