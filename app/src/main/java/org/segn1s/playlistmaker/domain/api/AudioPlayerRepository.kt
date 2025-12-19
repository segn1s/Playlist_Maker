package org.segn1s.playlistmaker.domain.api

interface AudioPlayerRepository {

    // Enum для состояний плеера, чтобы не использовать магические числа
    enum class PlayerState { DEFAULT, PREPARED, PLAYING, PAUSED }

    // Получить текущее состояние плеера
    fun getPlayerState(): PlayerState

    // Установить источник и подготовить плеер
    fun prepare(url: String?, onPrepared: () -> Unit, onCompletion: () -> Unit)

    // Управление воспроизведением
    fun start()
    fun pause()
    fun release()

    // Получить текущую позицию
    fun getCurrentPosition(): Long
}