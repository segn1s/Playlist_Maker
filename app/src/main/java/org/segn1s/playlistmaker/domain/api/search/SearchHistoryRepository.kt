package org.segn1s.playlistmaker.domain.api.search

import org.segn1s.playlistmaker.domain.model.Track

interface SearchHistoryRepository {

    // Добавляет трек в историю (с учетом лимита и порядка)
    fun addTrack(track: Track)

    // Получает всю историю треков
    fun getHistory(): List<Track>

    // Очищает всю историю
    fun clearHistory()
}