package org.segn1s.playlistmaker.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.segn1s.playlistmaker.R
import org.segn1s.playlistmaker.databinding.FragmentPlayerBinding
import org.segn1s.playlistmaker.domain.model.Track
import org.segn1s.playlistmaker.presentation.player.PlayerState
import org.segn1s.playlistmaker.presentation.player.PlayerViewModel

class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    // Получаем трек из аргументов навигации
    private val track by lazy {
        requireArguments().getSerializable(ARGS_TRACK) as Track
    }

    private val viewModel: PlayerViewModel by viewModel {
        parametersOf(track)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindTrackData(track)
        setupListeners()
        observeViewModel()

        // Инициализация плеера, если это не делает ViewModel сама при старте
        viewModel.preparePlayer(track.previewUrl.orEmpty())
    }

    private fun observeViewModel() {
        viewModel.playerState.observe(viewLifecycleOwner) { state ->
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
                    binding.playbackProgress.text = state.progress
                }
                is PlayerState.Paused -> {
                    binding.playPauseButton.setImageResource(R.drawable.ic_play_media)
                    binding.playbackProgress.text = state.progress
                }
            }
        }
    }

    private fun setupListeners() {
        // Навигация назад через NavController
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARGS_TRACK = "track"

        // Метод для удобного создания фрагмента (если не через NavGraph)
        fun createArgs(track: Track): Bundle =
            Bundle().apply {
                putSerializable(ARGS_TRACK, track)
            }
    }
}