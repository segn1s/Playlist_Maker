package org.segn1s.playlistmaker.data.repository

import android.media.MediaPlayer
import org.segn1s.playlistmaker.domain.api.AudioPlayerRepository

class AudioPlayerRepositoryImpl : AudioPlayerRepository {

    private val mediaPlayer = MediaPlayer()
    private var state: AudioPlayerRepository.PlayerState = AudioPlayerRepository.PlayerState.DEFAULT

    override fun getPlayerState(): AudioPlayerRepository.PlayerState = state

    override fun prepare(url: String?, onPrepared: () -> Unit, onCompletion: () -> Unit) {
        if (url.isNullOrEmpty()) {
            state = AudioPlayerRepository.PlayerState.DEFAULT
            return
        }

        mediaPlayer.setDataSource(url)
        mediaPlayer.prepareAsync()
        state = AudioPlayerRepository.PlayerState.DEFAULT

        mediaPlayer.setOnPreparedListener {
            state = AudioPlayerRepository.PlayerState.PREPARED
            onPrepared.invoke() // Вызываем колбэк для Интерактора/Activity
        }

        mediaPlayer.setOnCompletionListener {
            state = AudioPlayerRepository.PlayerState.PREPARED // Снова в состояние готовности
            onCompletion.invoke() // Вызываем колбэк для Интерактора/Activity
        }
    }

    override fun start() {
        mediaPlayer.start()
        state = AudioPlayerRepository.PlayerState.PLAYING
    }

    override fun pause() {
        mediaPlayer.pause()
        state = AudioPlayerRepository.PlayerState.PAUSED
    }

    override fun release() {
        mediaPlayer.release()
        state = AudioPlayerRepository.PlayerState.DEFAULT
    }

    override fun getCurrentPosition(): Long {
        return mediaPlayer.currentPosition.toLong()
    }
}