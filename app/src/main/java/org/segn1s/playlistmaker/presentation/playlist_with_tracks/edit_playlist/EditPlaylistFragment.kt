package org.segn1s.playlistmaker.presentation.playlist_with_tracks.edit_playlist

import org.segn1s.playlistmaker.presentation.media.playlists.creating.CreatePlaylistFragment
import org.segn1s.playlistmaker.presentation.media.playlists.creating.CreatePlaylistViewModel
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.segn1s.playlistmaker.R
import org.segn1s.playlistmaker.domain.model.Playlist
import java.io.File

class EditPlaylistFragment : CreatePlaylistFragment() {

    private val playlist: Playlist by lazy {
        requireArguments().getSerializable("playlist") as Playlist
    }

    val viewModel: CreatePlaylistViewModel by viewModel<EditPlaylistViewModel> {
        parametersOf(playlist)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Меняем заголовок и кнопку
        binding.createButton.text = getString(R.string.save)

        // Заполняем поля данными плейлиста
        binding.playlistName.setText(playlist.name)
        binding.playlistDescription.setText(playlist.description ?: "")

        // Загружаем обложку если есть
        val coverFile = playlist.coverPath?.let { File(it) }
        if (coverFile?.exists() == true) {
            selectedImageUri = Uri.fromFile(coverFile)
            if (coverImageView == null) {
                coverImageView = ImageView(requireContext()).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
                binding.coverContainer.addView(coverImageView, 0) // только один раз
                binding.coverContainer.clipToOutline = true
                binding.coverContainer.outlineProvider = android.view.ViewOutlineProvider.BACKGROUND
            }
            Glide.with(this)
                .load(coverFile)
                .centerCrop()
                .into(coverImageView!!)
            binding.addIcon.visibility = View.GONE
        }

        // Кнопка назад — без диалога
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            }
        )

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.createButton.setOnClickListener {
            val name = binding.playlistName.text.toString()
            val description = binding.playlistDescription.text.toString()
            viewModel.createPlaylist(name, description, selectedImageUri)
            Toast.makeText(requireContext(), getString(R.string.pl_saved), Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    companion object {
        fun createArgs(playlist: Playlist) = Bundle().apply {
            putSerializable("playlist", playlist)
        }
    }
}