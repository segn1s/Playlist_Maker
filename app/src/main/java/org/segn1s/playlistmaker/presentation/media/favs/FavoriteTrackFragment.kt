package org.segn1s.playlistmaker.presentation.media.favs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.segn1s.playlistmaker.R
import org.segn1s.playlistmaker.databinding.FragmentFavoriteTracksBinding
import org.segn1s.playlistmaker.domain.model.Track
import org.segn1s.playlistmaker.presentation.common.TrackAdapter

class FavoriteTracksFragment : Fragment() {

    private var _binding: FragmentFavoriteTracksBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavoriteTracksViewModel by viewModel()

    private val trackAdapter = TrackAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteTracksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        binding.recyclerViewFavorites.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFavorites.adapter = trackAdapter

        trackAdapter.setOnItemClickListener { track ->
            openPlayer(track)
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FavoriteTracksState.Empty -> {
                    binding.placeholderGroup.visibility = View.VISIBLE
                    binding.recyclerViewFavorites.visibility = View.GONE
                }
                is FavoriteTracksState.Content -> {
                    binding.placeholderGroup.visibility = View.GONE
                    binding.recyclerViewFavorites.visibility = View.VISIBLE
                    trackAdapter.updateData(state.tracks)
                }
            }
        }
    }

    private fun openPlayer(track: Track) {
        findNavController().navigate(
            R.id.playerFragment,
            android.os.Bundle().apply {
                putSerializable("track", track)
            }
        )
    }

    companion object {
        fun newInstance() = FavoriteTracksFragment()
    }
}