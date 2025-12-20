package org.segn1s.playlistmaker.presentation.player

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.segn1s.playlistmaker.domain.api.player.AudioPlayerInteractor
import org.segn1s.playlistmaker.domain.api.player.AudioPlayerRepository
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerViewModel(
    private val playerInteractor: AudioPlayerInteractor
) : ViewModel() {

    private val handler = Handler(Looper.getMainLooper())

    // Теперь это ЕДИНСТВЕННЫЙ источник истины для Activity
    private val _playerState = MutableLiveData<PlayerState>(PlayerState.Default)
    val playerState: LiveData<PlayerState> = _playerState

    private val updateProgressRunnable = object : Runnable {
        override fun run() {
            // Каждые 300мс создаем НОВОЕ состояние Playing с обновленным временем
            _playerState.postValue(PlayerState.Playing(formatMilliseconds(playerInteractor.getCurrentPosition())))
            handler.postDelayed(this, REFRESH_LISTENER_DELAY_MS)
        }
    }

    init {
        playerInteractor.setPlayerListener(
            onPrepared = {
                _playerState.postValue(PlayerState.Prepared)
            },
            onCompletion = {
                stopTimer()
                _playerState.postValue(PlayerState.Prepared)
            },
            onProgressUpdate = {}
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
        startTimer()
        // Состояние обновится через Runnable почти мгновенно
    }

    fun pausePlayer() {
        playerInteractor.pausePlayer()
        stopTimer()
        // При паузе фиксируем текущее время в состоянии Paused
        _playerState.postValue(PlayerState.Paused(formatMilliseconds(playerInteractor.getCurrentPosition())))
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

    companion object {
        private const val REFRESH_LISTENER_DELAY_MS = 300L
        private const val DEFAULT_TIMER_VALUE = "00:00"
    }
}