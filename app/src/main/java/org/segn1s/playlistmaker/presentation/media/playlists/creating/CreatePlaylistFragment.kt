package org.segn1s.playlistmaker.presentation.media.playlists.creating

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.segn1s.playlistmaker.R
import org.segn1s.playlistmaker.databinding.FragmentCreatePlaylistBinding

class CreatePlaylistFragment : Fragment() {

    private var _binding: FragmentCreatePlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreatePlaylistViewModel by viewModel()

    private var selectedImageUri: Uri? = null
    private var coverImageView: ImageView? = null

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                if (coverImageView == null) {
                    coverImageView = ImageView(requireContext()).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                    binding.coverContainer.addView(coverImageView, 0)
                }
                coverImageView?.setImageURI(uri)
                binding.addIcon.visibility = View.GONE
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.createButton.isEnabled = false

        // Восстановление URI после сворачивания
        savedInstanceState?.getString(KEY_URI)?.let { uriString ->
            val uri = Uri.parse(uriString)
            selectedImageUri = uri
            if (coverImageView == null) {
                coverImageView = ImageView(requireContext()).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
                binding.coverContainer.addView(coverImageView, 0)
            }
            coverImageView?.setImageURI(uri)
            binding.addIcon.visibility = View.GONE
        }

        binding.playlistName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val enabled = !s.isNullOrEmpty()
                binding.createButton.isEnabled = enabled
                binding.createButton.setBackgroundColor(
                    requireContext().getColor(
                        if (enabled) R.color.backgroundPlaylistButton
                        else R.color.ic_create_playlist_button
                    )
                )
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.coverContainer.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.createButton.setOnClickListener {
            val name = binding.playlistName.text.toString()
            val description = binding.playlistDescription.text.toString()
            viewModel.createPlaylist(name, description, selectedImageUri)
            Toast.makeText(requireContext(), getString(R.string.pl_created, binding.playlistName.text), Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)
        binding.backButton.setOnClickListener { handleBack() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        selectedImageUri?.let { outState.putString(KEY_URI, it.toString()) }
    }

    private fun hasUnsavedData(): Boolean {
        return selectedImageUri != null ||
                !binding.playlistName.text.isNullOrEmpty() ||
                !binding.playlistDescription.text.isNullOrEmpty()
    }

    private fun handleBack() {
        if (hasUnsavedData()) {
            val dialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.delete_playlist_confirm, binding.playlistName.text))
                .setNegativeButton(getString(R.string.pl_no)) { d, _ -> d.dismiss() }
                .setPositiveButton(getString(R.string.pl_yes)) { _, _ -> findNavController().popBackStack() }
                .create()

            dialog.show()

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(requireContext().getColor(R.color.backgroundPlaylistButton))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(requireContext().getColor(R.color.backgroundPlaylistButton))
        } else {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val KEY_URI = "selected_image_uri"
    }
}