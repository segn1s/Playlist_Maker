package org.segn1s.playlistmaker.presentation.media.playlists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.segn1s.playlistmaker.R
import org.segn1s.playlistmaker.databinding.FragmentPlaylistsBinding

class PlaylistsFragment : Fragment() {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlaylistsViewModel by viewModel()
    private lateinit var adapter: PlaylistAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = PlaylistAdapter { /* открытие плейлиста — позже */ }

        binding.playlistsRecycler.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.playlistsRecycler.adapter = adapter

        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            if (playlists.isEmpty()) {
                binding.placeholderImage.visibility = View.VISIBLE
                binding.placeholderText.visibility = View.VISIBLE
                binding.playlistsRecycler.visibility = View.GONE
            } else {
                binding.placeholderImage.visibility = View.GONE
                binding.placeholderText.visibility = View.GONE
                binding.playlistsRecycler.visibility = View.VISIBLE
                adapter.setItems(playlists)
            }
        }

        binding.newPlaylistButton.setOnClickListener {
            findNavController().navigate(R.id.action_mediaFragment_to_createPlaylistFragment)
        }

        viewModel.loadPlaylists()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadPlaylists() // перезагрузить после возврата с экрана создания
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = PlaylistsFragment()
    }
}