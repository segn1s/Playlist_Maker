package org.segn1s.playlistmaker.presentation.media.playlists.creating

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.segn1s.playlistmaker.R
import org.segn1s.playlistmaker.databinding.FragmentAddToPlaylistBottomSheetBinding
import org.segn1s.playlistmaker.domain.model.Track

class AddToPlaylistBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentAddToPlaylistBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val track by lazy {
        requireArguments().getSerializable(ARG_TRACK) as Track
    }

    private val viewModel: AddToPlaylistViewModel by viewModel {
        parametersOf(track)
    }

    private lateinit var adapter:
            BottomSheetPlaylistAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddToPlaylistBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = BottomSheetPlaylistAdapter { playlist ->
            viewModel.addTrackToPlaylist(playlist)
        }

        binding.playlistsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.playlistsRecycler.adapter = adapter

        viewModel.playlists.observe(viewLifecycleOwner) { adapter.setItems(it) }

        viewModel.addResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is AddToPlaylistResult.Added -> {
                    Toast.makeText(
                        requireContext(),
                        "Добавлено в плейлист ${result.playlistName}",
                        Toast.LENGTH_SHORT
                    ).show()
                    dismiss()
                }
                is AddToPlaylistResult.AlreadyExists -> {
                    Toast.makeText(
                        requireContext(),
                        "Трек уже добавлен в плейлист ${result.playlistName}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.newPlaylistButton.setOnClickListener {
            dismiss()
            requireParentFragment().findNavController()
                .navigate(R.id.action_playerFragment_to_createPlaylistFragment)
        }

        viewModel.loadPlaylists()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_TRACK = "track"

        fun newInstance(track: Track) = AddToPlaylistBottomSheet().apply {
            arguments = Bundle().apply { putSerializable(ARG_TRACK, track) }
        }
    }
}