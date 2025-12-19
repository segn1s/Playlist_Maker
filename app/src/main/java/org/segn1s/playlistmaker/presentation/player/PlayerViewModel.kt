package org.segn1s.playlistmaker.presentation.player

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.segn1s.playlistmaker.domain.api.player.AudioPlayerInteractor
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerViewModel(
    private val playerInteractor: AudioPlayerInteractor
) : ViewModel() {

    private val handler = Handler(Looper.getMainLooper())

    // Состояние плеера (экрана)
    private val _playerState = MutableLiveData<PlayerState>()
    val playerState: LiveData<PlayerState> = _playerState

    // Текущий прогресс
    private val _progress = MutableLiveData<String>()
    val progress: LiveData<String> = _progress

    private val updateProgressRunnable = object : Runnable {
        override fun run() {
            _progress.postValue(formatMilliseconds(playerInteractor.getCurrentPosition()))
            handler.postDelayed(this, REFRESH_LISTENER_DELAY_MS)
        }
    }

    init {
        // Подписываемся на события из Интерактора
        playerInteractor.setPlayerListener(
            onPrepared = {
                _playerState.postValue(PlayerState.PREPARED)
                _progress.postValue(DEFAULT_TIMER_VALUE)
            },
            onCompletion = {
                stopTimer()
                _playerState.postValue(PlayerState.PREPARED)
                _progress.postValue(DEFAULT_TIMER_VALUE)
            },
            onProgressUpdate = TODO()
        )
    }

    fun preparePlayer(url: String) {
        playerInteractor.preparePlayer(url)
    }

    fun playbackControl() {
        when (playerInteractor.getPlayerState()) {
            AudioPlayerInteractor.PlayerState.PLAYING -> pausePlayer()
            AudioPlayerInteractor.PlayerState.PREPARED, AudioPlayerInteractor.PlayerState.PAUSED -> startPlayer()
            else -> Unit
        }
    }

    private fun startPlayer() {
        playerInteractor.startPlayer()
        _playerState.postValue(PlayerState.PLAYING)
        startTimer()
    }

    fun pausePlayer() {
        playerInteractor.pausePlayer()
        _playerState.postValue(PlayerState.PAUSED)
        stopTimer()
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        playerInteractor.releasePlayer()
    }

    private fun startTimer() {
        handler.post(updateProgressRunnable)
    }

    private fun stopTimer() {
        handler.removeCallbacks(updateProgressRunnable)
    }

    private fun formatMilliseconds(millis: Long): String {
        return SimpleDateFormat("mm:ss", Locale.getDefault()).format(millis)
    }

    // Состояния для UI
    enum class PlayerState { DEFAULT, PREPARED, PLAYING, PAUSED }

    companion object {
        private const val REFRESH_LISTENER_DELAY_MS = 300L
        private const val DEFAULT_TIMER_VALUE = "00:00"
    }
}