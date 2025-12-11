package org.segn1s.playlistmaker.data.dto

import org.segn1s.playlistmaker.domain.model.Track

data class SearchResponse(
    val resultCount: Int,
    val results: List<TrackDto>
)