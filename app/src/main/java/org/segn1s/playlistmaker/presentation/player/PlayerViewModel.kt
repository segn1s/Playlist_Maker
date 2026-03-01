package org.segn1s.playlistmaker.presentation.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.segn1s.playlistmaker.domain.api.player.AudioPlayerInteractor
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerViewModel(
    private val playerInteractor: AudioPlayerInteractor
) : ViewModel() {

    private var progressJob: Job? = null

    private val _playerState = MutableLiveData<PlayerState>(PlayerState.Default)
    val playerState: LiveData<PlayerState> = _playerState

    init {
        playerInteractor.setPlayerListener(
            onPrepared = {
                _playerState.postValue(PlayerState.Prepared)
            },
            onCompletion = {
                stopProgress()
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
            AudioPlayerInteractor.PlayerState.PREPARED,
            AudioPlayerInteractor.PlayerState.PAUSED -> startPlayer()
            else -> Unit
        }
    }

    private fun startPlayer() {
        playerInteractor.startPlayer()
        startProgress()
    }

    fun pausePlayer() {
        playerInteractor.pausePlayer()
        stopProgress()
        _playerState.postValue(
            PlayerState.Paused(
                formatMilliseconds(playerInteractor.getCurrentPosition())
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        stopProgress()
        playerInteractor.releasePlayer()
    }

    private fun startProgress() {
        if (progressJob?.isActive == true) return

        progressJob = viewModelScope.launch {
            while (isActive) {
                _playerState.postValue(
                    PlayerState.Playing(
                        formatMilliseconds(playerInteractor.getCurrentPosition())
                    )
                )
                delay(REFRESH_LISTENER_DELAY_MS)
            }
        }
    }

    private fun stopProgress() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun formatMilliseconds(millis: Long): String {
        return SimpleDateFormat("mm:ss", Locale.getDefault()).format(millis)
    }

    companion object {
        private const val REFRESH_LISTENER_DELAY_MS = 300L
    }
}