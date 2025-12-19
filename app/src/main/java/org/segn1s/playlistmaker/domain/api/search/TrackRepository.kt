package org.segn1s.playlistmaker.domain.api.search

import org.segn1s.playlistmaker.domain.model.Track

interface TrackRepository {

    // Интерфейс для обратного вызова (Callback)
    interface TrackConsumer {
        fun consume(foundTracks: List<Track>, errorCode: Int?)
    }

    // Метод для поиска треков.
    // Вместо возврата результата он принимает Consumer для асинхронной обработки
    fun searchTracks(expression: String, consumer: TrackConsumer)
}