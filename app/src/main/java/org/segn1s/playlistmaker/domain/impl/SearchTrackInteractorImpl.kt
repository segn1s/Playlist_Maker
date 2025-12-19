package org.segn1s.playlistmaker.domain.impl

import org.segn1s.playlistmaker.domain.api.SearchTrackInteractor
import org.segn1s.playlistmaker.domain.api.TrackRepository
import org.segn1s.playlistmaker.domain.model.Track

class SearchTrackInteractorImpl(private val repository: TrackRepository) : SearchTrackInteractor {

    override fun searchTracks(expression: String, consumer: SearchTrackInteractor.TracksConsumer) {

        // Интерактор вызывает репозиторий и сам обрабатывает результат
        repository.searchTracks(expression, object : TrackRepository.TrackConsumer {

            override fun consume(foundTracks: List<Track>, errorCode: Int?) {
                if (errorCode != null) {
                    // Здесь может быть бизнес-логика обработки кодов ошибок (например,
                    // если ошибка -404, возвращаем пустой список, но флаг isFailed = true)
                    consumer.consume(emptyList(), true) // Передаем ошибку в Presentation
                } else {
                    // Если нужно отфильтровать или отсортировать результаты, это делается здесь
                    consumer.consume(foundTracks, false) // Передаем успешный результат
                }
            }
        })
    }
}