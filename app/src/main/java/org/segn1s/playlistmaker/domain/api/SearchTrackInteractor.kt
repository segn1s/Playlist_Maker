package org.segn1s.playlistmaker.domain.api

import org.segn1s.playlistmaker.domain.model.Track

interface SearchTrackInteractor {

    // Интерфейс для обратного вызова. В отличие от Repository Consumer,
    // здесь мы можем возвращать "красивые" статусы или enum-ы
    interface TracksConsumer {
        fun consume(foundTracks: List<Track>, isFailed: Boolean)
    }

    fun searchTracks(expression: String, consumer: TracksConsumer)
}