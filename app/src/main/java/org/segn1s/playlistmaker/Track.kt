package org.segn1s.playlistmaker

data class Track(
    val trackName: String, // Название трека
    val artistName: String, // Имя исполнителя
    val trackTime: String, // Продолжительность трека
    val artworkUrl100: String // Ссылка на изображение обложки
)