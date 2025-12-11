package org.segn1s.playlistmaker.presentation

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import org.segn1s.playlistmaker.R
import org.segn1s.playlistmaker.Creator
import org.segn1s.playlistmaker.domain.api.AudioPlayerInteractor
import org.segn1s.playlistmaker.domain.model.Track
import java.util.concurrent.TimeUnit

class PlayerActivity : AppCompatActivity() {

    // --- Интерактор ---
    private lateinit var playerInteractor: AudioPlayerInteractor

    // --- UI Components ---
    private lateinit var track: Track
    private lateinit var playPauseButton: ImageButton
    private lateinit var playbackProgress: TextView

    // --- Методы Жизненного Цикла ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        // ❗ Инициализация Интерактора
        playerInteractor = Creator.provideAudioPlayerInteractor()

        track = intent.getSerializableExtra("track_extra") as? Track ?: run {
            finish()
            return
        }

        // ... (Инициализация Views) ...

        playPauseButton = findViewById(R.id.playPauseButton)
        playbackProgress = findViewById(R.id.playback_progress)

        // Привязка данных (Ваш существующий код)
        // ...

        // ❗ Привязка данных к UI остается в Activity
        bindTrackDataToViews(track)

        // ❗ 1. Настраиваем слушатели и готовим плеер (через Интерактор)
        setupPlayerListener()
        playerInteractor.preparePlayer(track.previewUrl.orEmpty())

        // ❗ 2. Обработка нажатия кнопки (делегирование Интерактору)
        playPauseButton.setOnClickListener {
            playerInteractor.playbackControl()
            updatePlayPauseButton()
        }
    }

    override fun onPause() {
        super.onPause()
        // ❗ Приостановка через Интерактор
        playerInteractor.pausePlayer()
        updatePlayPauseButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        // ❗ Освобождение ресурсов через Интерактор
        playerInteractor.releasePlayer()
    }

    // --- Логика UI (Обновление иконок и текста) ---

    private fun updatePlayPauseButton() {
        when (playerInteractor.getPlayerState()) {
            AudioPlayerInteractor.PlayerState.PLAYING -> {
                playPauseButton.setImageResource(R.drawable.ic_paused_media)
            }
            AudioPlayerInteractor.PlayerState.PAUSED, AudioPlayerInteractor.PlayerState.PREPARED -> {
                playPauseButton.setImageResource(R.drawable.ic_play_media)
            }
            else -> {
                playPauseButton.isEnabled = false // Если DEFAULT, то кнопка не активна
            }
        }
    }

    private fun setupPlayerListener() {
        // ❗ Регистрируем колбэки:
        playerInteractor.setPlayerListener(
            onPrepared = {
                // Плеер готов: включаем кнопку и устанавливаем начальное состояние
                playPauseButton.isEnabled = true
                playbackProgress.text = "00:00"
                updatePlayPauseButton()
            },
            onCompletion = {
                // Воспроизведение завершено: обновляем UI и состояние
                playbackProgress.text = "00:00"
                updatePlayPauseButton()
            },
            onProgressUpdate = { position ->
                // Обновление прогресса
                playbackProgress.text = formatMilliseconds(position)
            }
        )
    }

    private fun bindTrackDataToViews(track: Track) {
        // ... (Код привязки данных к TextView и ImageView остается здесь) ...

        val backButton = findViewById<ImageButton>(R.id.back_button)
        val albumCover = findViewById<ImageView>(R.id.album_cover)
        val trackName = findViewById<TextView>(R.id.track_name)
        val artistName = findViewById<TextView>(R.id.artist_name)
        val tvDuration = findViewById<TextView>(R.id.track_duration_data)
        val tvAlbum = findViewById<TextView>(R.id.album_info_data)
        val tvYear = findViewById<TextView>(R.id.release_year_data)
        val tvGenre = findViewById<TextView>(R.id.genre_info_data)
        val tvCountry = findViewById<TextView>(R.id.country_info_data)

        trackName.text = track.trackName
        artistName.text = track.artistName

        tvDuration.text = track.trackTime ?: "--:--"
        tvAlbum.text = track.collectionName ?: getString(R.string.album)
        tvYear.text = track.releaseDate?.take(4) ?: getString(R.string.year)
        tvGenre.text = track.primaryGenreName ?: getString(R.string.genre)
        tvCountry.text = track.country ?: getString(R.string.country)

        val highResUrl = track.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")
        val cornerRadius = resources.getDimensionPixelSize(R.dimen.album_corner_radius)

        Glide.with(this)
            .load(highResUrl)
            .transform(CenterCrop(), RoundedCorners(cornerRadius))
            .placeholder(R.drawable.placeholder)
            .into(albumCover)

        backButton.setOnClickListener { finish() }
    }


    // --- Утилиты (остаются в Presentation) ---

    /** Форматирует миллисекунды в строку формата mm:ss */
    private fun formatMilliseconds(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) -
                TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, seconds)
    }
}