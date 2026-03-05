package org.segn1s.playlistmaker.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.segn1s.playlistmaker.data.ITunesApi
import org.segn1s.playlistmaker.data.db.FavoriteTrackDao
import org.segn1s.playlistmaker.data.dto.SearchResponse
import org.segn1s.playlistmaker.data.dto.toDomainTrack
import org.segn1s.playlistmaker.domain.api.search.TrackRepository
import retrofit2.HttpException
import java.io.IOException

class TrackRepositoryImpl(
    private val api: ITunesApi,
    private val favoriteTrackDao: FavoriteTrackDao
) : TrackRepository {

    override fun searchTracks(expression: String): Flow<TrackRepository.SearchResult> = flow {
        try {
            val response = api.searchSongs(expression)

            val favoriteIds = favoriteTrackDao.getAllFavoriteIds().toSet()

            val tracks = response.results.map { dto ->
                val track = dto.toDomainTrack()
                track.isFavorite = track.trackId in favoriteIds
                track
            }

            emit(TrackRepository.SearchResult.Success(tracks))

        } catch (e: HttpException) {

            emit(
                TrackRepository.SearchResult.Error(
                    errorCode = e.code(),
                    message = e.message()
                )
            )

        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {

            emit(
                TrackRepository.SearchResult.Error(
                    errorCode = -1,
                    message = e.message
                )
            )
        }
    }.flowOn(Dispatchers.IO)
}