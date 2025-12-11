package org.segn1s.playlistmaker.domain

import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.segn1s.playlistmaker.domain.model.Track

class SearchHistory(private val sharedPreferences: SharedPreferences) {

    private val gson = Gson()
    private val key = "SEARCH_HISTORY"
    private val maxSize = 10

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun addTrack(track: Track) {
        val history = getHistory().toMutableList()

        val alreadyExists = history.any { it.trackId == track.trackId }

        // Если такой трек уже есть — переместить наверх
        if (alreadyExists) {
            history.removeAll { it.trackId == track.trackId }
            history.add(0, track)
            saveHistory(history)
            return
        }

        // Если трека нет и достигнут максимум — просто не добавлять 11-й
        if (history.size >= maxSize) {
            return
        }

        // Добавить новый в начало (размер гарантированно < maxSize)
        history.add(0, track)
        saveHistory(history)
    }

    fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(key, null) ?: return emptyList()
        val type = object : TypeToken<List<Track>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun clearHistory() {
        sharedPreferences.edit().remove(key).apply()
    }

    private fun saveHistory(history: List<Track>) {
        val json = gson.toJson(history)
        sharedPreferences.edit().putString(key, json).apply()
    }
}