package org.segn1s.playlistmaker

import java.text.SimpleDateFormat
import java.util.Locale

data class Track(
    val trackName: String, // Название трека
    val artistName: String, // Имя исполнителя
    val trackTimeMillis: Long?, // Продолжительность трека
    val artworkUrl100: String // Ссылка на изображение обложки
) {
    val trackTime: String?
        get() {
            return if (trackTimeMillis != null) {
                val formattedTime = SimpleDateFormat("mm:ss", Locale.getDefault()).format(trackTimeMillis)
                formattedTime
            } else {
                "--:--"
            }
        }
}