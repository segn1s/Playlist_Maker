package org.segn1s.playlistmaker.domain.impl

import android.os.Handler
import android.os.Looper
import org.segn1s.playlistmaker.domain.api.AudioPlayerInteractor
import org.segn1s.playlistmaker.domain.api.AudioPlayerRepository

class AudioPlayerInteractorImpl(
    private val repository: AudioPlayerRepository
) : AudioPlayerInteractor {

    private var onProgressUpdate: ((Long) -> Unit)? = null
    private var onCompletion: (() -> Unit)? = null
    private var onPrepared: (() -> Unit)? = null

    private val handler = Handler(Looper.getMainLooper())
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            if (repository.getPlayerState() == AudioPlayerRepository.PlayerState.PLAYING) {
                // Вызываем колбэк для обновления UI
                onProgressUpdate?.invoke(repository.getCurrentPosition())
                handler.postDelayed(this, 500L) // DELAY_MILLIS
            }
        }
    }

    override fun setPlayerListener(
        onPrepared: () -> Unit,
        onCompletion: () -> Unit,
        onProgressUpdate: (Long) -> Unit
    ) {
        this.onPrepared = onPrepared
        this.onCompletion = onCompletion
        this.onProgressUpdate = onProgressUpdate
    }

    override fun preparePlayer(url: String) {
        repository.prepare(
            url = url,
            onPrepared = { onPrepared?.invoke() },
            onCompletion = {
                handler.removeCallbacks(updateTimeRunnable)
                onCompletion?.invoke()
            }
        )
    }

    override fun playbackControl() {
        when (repository.getPlayerState()) {
            AudioPlayerRepository.PlayerState.PLAYING -> pausePlayer()
            AudioPlayerRepository.PlayerState.PAUSED, AudioPlayerRepository.PlayerState.PREPARED -> startPlayer()
            else -> {}
        }
    }

    override fun startPlayer() {
        repository.start()
        handler.post(updateTimeRunnable) // Запуск обновления времени
    }

    override fun pausePlayer() {
        repository.pause()
        handler.removeCallbacks(updateTimeRunnable) // Остановка обновления времени
    }

    override fun releasePlayer() {
        handler.removeCallbacks(updateTimeRunnable)
        repository.release()
    }

    override fun getPlayerState(): AudioPlayerInteractor.PlayerState {
        // Конвертация из репозиторного enum в интеракторный enum
        return when (repository.getPlayerState()) {
            AudioPlayerRepository.PlayerState.DEFAULT -> AudioPlayerInteractor.PlayerState.DEFAULT
            AudioPlayerRepository.PlayerState.PREPARED -> AudioPlayerInteractor.PlayerState.PREPARED
            AudioPlayerRepository.PlayerState.PLAYING -> AudioPlayerInteractor.PlayerState.PLAYING
            AudioPlayerRepository.PlayerState.PAUSED -> AudioPlayerInteractor.PlayerState.PAUSED
        }
    }
}