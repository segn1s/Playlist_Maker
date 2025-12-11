package org.segn1s.playlistmaker.data.dto

import org.segn1s.playlistmaker.domain.model.Track // Обратите внимание на новый путь к Track

// Используем аннотацию @SerializedName, если названия полей API отличаются от Kotlin-стиля
data class TrackDto(
    val trackId: Long,
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Long?,
    val artworkUrl100: String,
    val collectionName: String?,
    val releaseDate: String?,
    val primaryGenreName: String?,
    val country: String?,
    val previewUrl: String?
)

// Функция-расширение для преобразования DTO в доменную модель (для использования в RepositoryImpl)
fun TrackDto.toDomainTrack(): Track {
    return Track(
        trackId = this.trackId,
        trackName = this.trackName,
        artistName = this.artistName,
        trackTimeMillis = this.trackTimeMillis,
        artworkUrl100 = this.artworkUrl100,
        collectionName = this.collectionName,
        releaseDate = this.releaseDate,
        primaryGenreName = this.primaryGenreName,
        country = this.country,
        previewUrl = this.previewUrl
    )
}