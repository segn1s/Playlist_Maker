package org.segn1s.playlistmaker.data.repository

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.segn1s.playlistmaker.domain.api.search.SearchHistoryRepository
import org.segn1s.playlistmaker.domain.model.Track

class SearchHistoryRepositoryImpl(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson // Используем Gson для сериализации
) : SearchHistoryRepository {

    companion object {
        private const val HISTORY_KEY = "search_history_key"
        private const val HISTORY_MAX_SIZE = 10 // Лимит на 10 треков в истории
    }

    override fun addTrack(track: Track) {
        val history = getHistory().toMutableList()

        // 1. Удаляем трек, если он уже есть (для перемещения его наверх)
        history.removeIf { it.trackId == track.trackId }

        // 2. Добавляем новый трек в начало списка
        history.add(0, track)

        // 3. Обрезаем список до максимального размера
        val limitedHistory = history.take(HISTORY_MAX_SIZE)

        // 4. Сохраняем обновленный список в SharedPreferences
        sharedPreferences.edit()
            .putString(HISTORY_KEY, gson.toJson(limitedHistory))
            .apply()
    }

    override fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(HISTORY_KEY, null) ?: return emptyList()

        // Десериализация списка треков
        val type = object : TypeToken<List<Track>>() {}.type
        return gson.fromJson(json, type)
    }

    override fun clearHistory() {
        sharedPreferences.edit()
            .remove(HISTORY_KEY)
            .apply()
    }
}