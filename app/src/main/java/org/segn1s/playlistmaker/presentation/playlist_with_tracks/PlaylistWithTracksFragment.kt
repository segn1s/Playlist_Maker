package org.segn1s.playlistmaker.presentation.media.playlists

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.segn1s.playlistmaker.R
import org.segn1s.playlistmaker.databinding.FragmentPlaylistWithTracksBinding
import org.segn1s.playlistmaker.domain.model.Track
import org.segn1s.playlistmaker.presentation.playlist_with_tracks.PlaylistWithTracksViewModel
import org.segn1s.playlistmaker.presentation.playlist_with_tracks.TracksInPlaylistAdapter
import org.segn1s.playlistmaker.presentation.playlist_with_tracks.edit_playlist.EditPlaylistFragment
import org.segn1s.playlistmaker.ui.player.PlayerFragment
import java.io.File

class PlaylistWithTracksFragment : Fragment() {

    private var _binding: FragmentPlaylistWithTracksBinding? = null
    private val binding get() = _binding!!

    private lateinit var menuBottomSheetBehavior: BottomSheetBehavior<View>

    private val playlistId: Int by lazy {
        requireArguments().getInt("playlistId")
    }

    private val viewModel: PlaylistWithTracksViewModel by viewModel {
        parametersOf(playlistId)
    }

    private lateinit var tracksAdapter: TracksInPlaylistAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistWithTracksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomSheet()
        setupMenuBottomSheet()
        setupRecycler()
        setupObservers()
        setupListeners()

        viewModel.loadPlaylist()
    }

    private fun setupBottomSheet() {
        val behavior = BottomSheetBehavior.from(binding.tracksBottomSheet)
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        behavior.isHideable = false
    }

    private fun setupRecycler() {
        tracksAdapter = TracksInPlaylistAdapter(
            onTrackClick = { track ->
                findNavController().navigate(
                    R.id.action_playlistWithTracksFragment_to_playerFragment,
                    PlayerFragment.createArgs(track)
                )
            },
            onTrackLongClick = { track -> showDeleteTrackDialog(track) }
        )
        binding.tracksRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.tracksRecycler.adapter = tracksAdapter
    }

    private fun setupObservers() {
        viewModel.playlist.observe(viewLifecycleOwner) { playlist ->
            binding.playlistName.text = playlist.name

            if (!playlist.description.isNullOrBlank()) {
                binding.playlistDescription.visibility = View.VISIBLE
                binding.playlistDescription.text = playlist.description
            } else {
                binding.playlistDescription.visibility = View.GONE
            }

            val coverFile = playlist.coverPath?.let { File(it) }
            val hasCover = coverFile?.exists() == true

            // Белая подложка если есть обложка, цвет фона если нет
            binding.coverCard.setCardBackgroundColor(
                if (hasCover) requireContext().getColor(android.R.color.white)
                else requireContext().getColor(R.color.coverBackground)
            )

            Glide.with(this)
                .load(if (hasCover) coverFile else null)
                .placeholder(R.drawable.placeholder)
                .centerCrop()
                .into(binding.playlistCover)
        }

        viewModel.tracks.observe(viewLifecycleOwner) { tracks ->
            tracksAdapter.setItems(tracks)
            binding.emptyTracksText.visibility = if (tracks.isEmpty()) View.VISIBLE else View.GONE
            binding.tracksRecycler.visibility = if (tracks.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.totalMinutes.observe(viewLifecycleOwner) { minutes ->
            val playlist = viewModel.playlist.value ?: return@observe
            binding.playlistStats.text = "${minutes.toMinutesString()} • ${playlist.tracksCount.toTracksString()}"
        }
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.shareButton.setOnClickListener {
            sharePlaylist()
        }

        binding.menuButton.setOnClickListener {
            showMenuBottomSheet()
        }
    }

    private fun showDeleteTrackDialog(track: Track) {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_track))
            .setNegativeButton(getString(R.string.pl_no)) { d, _ -> d.dismiss() }
            .setPositiveButton(getString(R.string.pl_yes)) { _, _ -> viewModel.deleteTrackFromPlaylist(track) }
            .create()

        dialog.show()
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            .setTextColor(requireContext().getColor(R.color.backgroundPlaylistButton))
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
            .setTextColor(requireContext().getColor(R.color.backgroundPlaylistButton))
    }

    private fun Int.toTracksString(): String {
        val lastTwo = this % 100
        val lastOne = this % 10
        return when {
            lastTwo in 11..19 -> "$this " + getString(R.string.track_count)
            lastOne == 1 -> "$this " + getString(R.string.track_count1)
            lastOne in 2..4 ->"$this " + getString(R.string.track_count2_4)
            else -> "$this " + getString(R.string.track_count)
        }
    }

    private fun Long.toMinutesString(): String {
        val lastTwo = this % 100
        val lastOne = this % 10
        return when {
            lastTwo in 11L..19L -> "$this " + getString(R.string.min_count)
            lastOne == 1L -> "$this " + getString(R.string.min_count1)
            lastOne in 2L..4L -> "$this " + getString(R.string.min_count2_4)
            else -> "$this " + getString(R.string.min_count)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadPlaylist()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun sharePlaylist() {
        val tracks = viewModel.tracks.value ?: emptyList()
        val playlist = viewModel.playlist.value ?: return

        if (tracks.isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.theres_no_tracks),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val sb = StringBuilder()
        sb.appendLine(playlist.name)
        if (!playlist.description.isNullOrBlank()) sb.appendLine(playlist.description)
        sb.appendLine(playlist.tracksCount.toTracksString())
        tracks.forEachIndexed { index, track ->
            sb.appendLine("${index + 1}. ${track.artistName} - ${track.trackName} (${track.trackTime})")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }
        startActivity(Intent.createChooser(intent, null))
    }

    private fun setupMenuBottomSheet() {
        menuBottomSheetBehavior = BottomSheetBehavior.from(binding.menuBottomSheet.root)
        menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        menuBottomSheetBehavior.peekHeight = 0
        menuBottomSheetBehavior.isHideable = true

        // Ограничиваем высоту — 324dp от низа экрана
        val expandedOffsetPx = resources.displayMetrics.heightPixels -
                (324 * resources.displayMetrics.density).toInt()
        menuBottomSheetBehavior.expandedOffset = expandedOffsetPx

        menuBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    binding.dimOverlay.visibility = View.GONE
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        binding.dimOverlay.setOnClickListener {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun showMenuBottomSheet() {
        val playlist = viewModel.playlist.value ?: return
        val coverFile = playlist.coverPath?.let { File(it) }

        Glide.with(this)
            .load(if (coverFile?.exists() == true) coverFile else null)
            .placeholder(R.drawable.placeholder)
            .centerCrop()
            .into(binding.menuBottomSheet.menuPlaylistCover)

        binding.menuBottomSheet.menuPlaylistName.text = playlist.name
        binding.menuBottomSheet.menuPlaylistTracksCount.text = playlist.tracksCount.toTracksString()

        binding.menuBottomSheet.menuShare.setOnClickListener {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            sharePlaylist()
        }

        binding.menuBottomSheet.menuEdit.setOnClickListener {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            val p = viewModel.playlist.value ?: return@setOnClickListener
            findNavController().navigate(
                R.id.action_playlistWithTracksFragment_to_editPlaylistFragment,
                EditPlaylistFragment.createArgs(p)
            )
        }

        binding.menuBottomSheet.menuDelete.setOnClickListener {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            showDeletePlaylistDialog()
        }

        binding.dimOverlay.visibility = View.VISIBLE
        menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun showDeletePlaylistDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_playlist))
            .setMessage(getString(R.string.delete_playlist_message2))
            .setNegativeButton(getString(R.string.delete_playlist_cancel)) { d, _ -> d.dismiss() }
            .setPositiveButton(getString(R.string.delete_playlist_delete)) { _, _ ->
                viewModel.deletePlaylist {
                    findNavController().popBackStack()
                }
            }
            .create()

        dialog.show()
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            .setTextColor(requireContext().getColor(R.color.backgroundPlaylistButton))
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
            .setTextColor(requireContext().getColor(R.color.backgroundPlaylistButton))
    }
}