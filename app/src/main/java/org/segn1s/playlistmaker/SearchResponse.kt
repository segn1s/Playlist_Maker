package org.segn1s.playlistmaker

data class SearchResponse(
    val resultCount: Int,
    val results: List<Track>
)