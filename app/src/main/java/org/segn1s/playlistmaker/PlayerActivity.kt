package org.segn1s.playlistmaker

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.io.Serializable
import java.util.concurrent.TimeUnit

class PlayerActivity : AppCompatActivity() {

    // --- Константы и Состояние Плеера ---
    companion object {
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
        private const val DELAY_MILLIS = 500L // Интервал обновления прогресса
    }

    private var playerState = STATE_DEFAULT
    private val mediaPlayer = MediaPlayer()

    // UI элементы для управления
    private lateinit var track: Track
    private lateinit var playPauseButton: ImageButton
    private lateinit var playbackProgress: TextView

    // Handler для обновления времени воспроизведения
    private val handler = Handler(Looper.getMainLooper())
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            if (playerState == STATE_PLAYING) {
                // Обновляем текст прогресса
                val currentPosition = mediaPlayer.currentPosition.toLong()
                playbackProgress.text = formatMilliseconds(currentPosition)
                // Повторяем задачу через DELAY_MILLIS
                handler.postDelayed(this, DELAY_MILLIS)
            }
        }
    }

    // --- Методы Жизненного Цикла ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        // Получение данных трека. Используем safe cast и return, если данных нет.
        track = intent.getSerializableExtra("track_extra") as? Track ?: run {
            finish()
            return
        }

        // Инициализация Views (Ваш существующий код)
        val backButton = findViewById<ImageButton>(R.id.back_button)
        val albumCover = findViewById<ImageView>(R.id.album_cover)
        val trackName = findViewById<TextView>(R.id.track_name)
        val artistName = findViewById<TextView>(R.id.artist_name)

        val tvDuration = findViewById<TextView>(R.id.track_duration_data)
        val tvAlbum = findViewById<TextView>(R.id.album_info_data)
        val tvYear = findViewById<TextView>(R.id.release_year_data)
        val tvGenre = findViewById<TextView>(R.id.genre_info_data)
        val tvCountry = findViewById<TextView>(R.id.country_info_data)

        playPauseButton = findViewById(R.id.playPauseButton)
        playbackProgress = findViewById(R.id.playback_progress)

        // Обработка кнопки "Назад"
        backButton.setOnClickListener { finish() }

        // Привязка данных (Ваш существующий код)
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

        // Инициализация плеера и установка слушателя нажатий
        preparePlayer()

        playPauseButton.setOnClickListener {
            playbackControl()
        }
    }

    override fun onPause() {
        super.onPause()
        // Критерий: При переводе в фон приостанавливаем воспроизведение
        if (playerState == STATE_PLAYING) {
            pausePlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release() // Освобождение ресурсов плеера
        handler.removeCallbacks(updateTimeRunnable) // Остановка задач Handler
    }

    // При системном нажатии "Назад" также останавливаем плеер (finish() вызывает onDestroy)
    override fun finish() {
        if (playerState == STATE_PLAYING) {
            // Если воспроизведение идет, сначала приостанавливаем его
            mediaPlayer.pause()
        }
        super.finish()
    }

    // --- Методы Управления Плеером ---

    private fun preparePlayer() {
        val url = track.previewUrl

        // Если ссылка на отрывок пустая, отключаем кнопку
        if (url.isNullOrEmpty()) {
            playPauseButton.isEnabled = false
            return
        }

        mediaPlayer.setDataSource(url)
        mediaPlayer.prepareAsync()

        // 1. Когда плеер готов к воспроизведению
        mediaPlayer.setOnPreparedListener {
            playerState = STATE_PREPARED
            playPauseButton.isEnabled = true
            playbackProgress.text = "00:00"
        }

        // 2. Когда воспроизведение закончилось
        mediaPlayer.setOnCompletionListener {
            handler.removeCallbacks(updateTimeRunnable)
            playbackProgress.text = "00:00"
            playPauseButton.setImageResource(R.drawable.ic_play_media)
            playerState = STATE_PREPARED // Возвращаем в состояние готовности для повторного проигрывания
        }
    }

    private fun playbackControl() {
        when (playerState) {
            STATE_PLAYING -> pausePlayer()
            STATE_PAUSED, STATE_PREPARED -> startPlayer()
        }
    }

    private fun startPlayer() {
        mediaPlayer.start()
        playPauseButton.setImageResource(R.drawable.ic_paused_media) // Установка иконки Пауза
        playerState = STATE_PLAYING
        handler.post(updateTimeRunnable) // Запуск обновления времени
    }

    private fun pausePlayer() {
        mediaPlayer.pause()
        playPauseButton.setImageResource(R.drawable.ic_play_media) // Установка иконки Играть
        playerState = STATE_PAUSED
        handler.removeCallbacks(updateTimeRunnable) // Остановка обновления времени
    }

    // --- Утилиты ---

    /** Форматирует миллисекунды в строку формата mm:ss */
    private fun formatMilliseconds(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) -
                TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, seconds)
    }
}