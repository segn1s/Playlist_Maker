package org.segn1s.playlistmaker.domain.impl

import org.segn1s.playlistmaker.domain.api.player.AudioPlayerInteractor
import org.segn1s.playlistmaker.domain.api.player.AudioPlayerRepository

class AudioPlayerInteractorImpl(
    private val repository: AudioPlayerRepository
) : AudioPlayerInteractor {

    private var onCompletion: (() -> Unit)? = null
    private var onPrepared: (() -> Unit)? = null

    override fun setPlayerListener(
        onPrepared: () -> Unit,
        onCompletion: () -> Unit,
        onProgressUpdate: (Long) -> Unit
    ) {
        this.onPrepared = onPrepared
        this.onCompletion = onCompletion
        // onProgressUpdate больше не используется, так как прогресс обновляется через корутину в ViewModel
    }

    override fun preparePlayer(url: String) {
        repository.prepare(
            url = url,
            onPrepared = { onPrepared?.invoke() },
            onCompletion = {
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
    }

    override fun pausePlayer() {
        repository.pause()
    }

    override fun releasePlayer() {
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

    override fun getCurrentPosition(): Long {
        return repository.getCurrentPosition()
    }
}