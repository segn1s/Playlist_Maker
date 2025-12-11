package org.segn1s.playlistmaker.data

import org.segn1s.playlistmaker.data.dto.SearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ITunesApi {
    @GET("/search?entity=song")
    fun searchSongs(@Query("term") text: String): Call<SearchResponse>
}