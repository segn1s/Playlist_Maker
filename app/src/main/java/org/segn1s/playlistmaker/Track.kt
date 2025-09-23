package org.segn1s.playlistmaker

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Locale

data class Track(
    val trackId: Long,
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Long?,
    val artworkUrl100: String,
    val collectionName: String? = null,
    val releaseDate: String? = null,
    val primaryGenreName: String? = null,
    val country: String? = null
) : Serializable {
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