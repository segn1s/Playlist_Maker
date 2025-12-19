package org.segn1s.playlistmaker.data.repository

import org.segn1s.playlistmaker.data.ITunesApi
import org.segn1s.playlistmaker.data.dto.SearchResponse
import org.segn1s.playlistmaker.data.dto.toDomainTrack
import org.segn1s.playlistmaker.domain.api.TrackRepository
import org.segn1s.playlistmaker.domain.model.Track
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Константы для кодов ошибок
private const val HTTP_STATUS_OK = 200
private const val NO_CONNECTION_ERROR = -1

class TrackRepositoryImpl(private val api: ITunesApi) : TrackRepository {

    override fun searchTracks(expression: String, consumer: TrackRepository.TrackConsumer) {
        api.searchSongs(expression).enqueue(object : Callback<SearchResponse> {
            override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                when (response.code()) {
                    HTTP_STATUS_OK -> {
                        val tracks = response.body()?.results?.map { it.toDomainTrack() } ?: emptyList()
                        consumer.consume(tracks, null) // Успех
                    }
                    else -> {
                        // Другие HTTP-ошибки
                        consumer.consume(emptyList(), response.code())
                    }
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                // Ошибка сети/соединения
                consumer.consume(emptyList(), NO_CONNECTION_ERROR)
            }
        })
    }
}
