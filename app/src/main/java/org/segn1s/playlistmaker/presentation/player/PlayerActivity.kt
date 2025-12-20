package org.segn1s.playlistmaker.presentation.player

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.segn1s.playlistmaker.R
import org.segn1s.playlistmaker.databinding.ActivityPlayerBinding
import org.segn1s.playlistmaker.domain.model.Track

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private val viewModel: PlayerViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val track = intent.getSerializableExtra("track_extra") as? Track ?: run {
            finish()
            return
        }

        bindTrackData(track)
        setupListeners()
        observeViewModel()

        viewModel.preparePlayer(track.previewUrl.orEmpty())
    }

    private fun observeViewModel() {
        // Следим за состоянием плеера (иконка кнопки)
        viewModel.playerState.observe(this) { state ->
            when (state) {
                is PlayerState.Default -> {
                    binding.playPauseButton.isEnabled = false
                    binding.playbackProgress.text = "00:00"
                }
                is PlayerState.Prepared -> {
                    binding.playPauseButton.isEnabled = true
                    binding.playPauseButton.setImageResource(R.drawable.ic_play_media)
                    binding.playbackProgress.text = "00:00"
                }
                is PlayerState.Playing -> {
                    binding.playPauseButton.setImageResource(R.drawable.ic_paused_media)
                    binding.playbackProgress.text = state.progress // Берем прогресс из стейта!
                }
                is PlayerState.Paused -> {
                    binding.playPauseButton.setImageResource(R.drawable.ic_play_media)
                    binding.playbackProgress.text = state.progress // Берем прогресс из стейта!
                }
            }
        }
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener { finish() }
        binding.playPauseButton.setOnClickListener {
            viewModel.playbackControl()
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.pausePlayer()
    }

    private fun bindTrackData(track: Track) {
        with(binding) {
            trackName.text = track.trackName
            artistName.text = track.artistName
            trackDurationData.text = track.trackTime
            albumInfoData.text = track.collectionName
            releaseYearData.text = track.releaseDate?.take(4)
            genreInfoData.text = track.primaryGenreName
            countryInfoData.text = track.country
        }

        val highResUrl = track.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")
        Glide.with(this)
            .load(highResUrl)
            .transform(
                CenterCrop(),
                RoundedCorners(resources.getDimensionPixelSize(R.dimen.album_corner_radius))
            )
            .placeholder(R.drawable.placeholder)
            .into(binding.albumCover)
    }
}