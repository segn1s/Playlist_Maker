package org.segn1s.playlistmaker.domain.api.player

interface AudioPlayerInteractor {

    enum class PlayerState { DEFAULT, PREPARED, PLAYING, PAUSED }

    // Основные методы
    fun preparePlayer(url: String)
    fun playbackControl() // Запуск/Пауза
    fun startPlayer()
    fun pausePlayer()
    fun releasePlayer()

    // Получение состояния
    fun getPlayerState(): PlayerState

    // Регистрация слушателя прогресса и состояния
    fun setPlayerListener(
        onPrepared: () -> Unit,
        onCompletion: () -> Unit,
        onProgressUpdate: (Long) -> Unit
    )

    fun getCurrentPosition(): Long
}