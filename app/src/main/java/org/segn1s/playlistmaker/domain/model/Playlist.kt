package org.segn1s.playlistmaker.domain.model

data class Playlist(
    val id: Int,
    val name: String,
    val description: String?,
    val coverPath: String?,
    val trackIds: List<Int>,
    val tracksCount: Int
)