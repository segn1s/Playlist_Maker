package org.segn1s.playlistmaker.presentation.search

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.segn1s.playlistmaker.R
import org.segn1s.playlistmaker.databinding.FragmentSearchBinding
import org.segn1s.playlistmaker.domain.model.Track
import org.segn1s.playlistmaker.presentation.common.TrackAdapter

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModel()

    private val trackAdapter = TrackAdapter()
    private val historyAdapter = TrackAdapter()

    private var textWatcher: TextWatcher? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        setupListeners()

        // ВАЖНО: Используем viewLifecycleOwner для подписки во фрагментах
        viewModel.stateLiveData.observe(viewLifecycleOwner) { state ->
            renderState(state)
        }
    }

    private fun setupAdapters() {
        // Используем requireContext() вместо this
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = trackAdapter

        binding.historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.historyRecyclerView.adapter = historyAdapter
    }

    private fun setupListeners() {
        binding.clearButton.setOnClickListener {
            binding.searchEditText.text.clear()
            hideKeyboard()
            viewModel.showHistory()
        }

        binding.clearHistoryButton.setOnClickListener {
            viewModel.clearHistory()
        }

        binding.buttonUpdate.setOnClickListener {
            viewModel.performSearch(binding.searchEditText.text.toString())
        }

        // Клик по результатам поиска
        trackAdapter.setOnItemClickListener { track ->
            viewModel.addTrackToHistory(track)
            openPlayer(track)
        }

        // Клик по истории
        historyAdapter.setOnItemClickListener { track ->
            viewModel.addTrackToHistory(track)
            openPlayer(track)
        }

        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                binding.clearButton.visibility = if (query.isEmpty()) View.GONE else View.VISIBLE

                if (query.isEmpty()) {
                    viewModel.showHistory()
                } else {
                    viewModel.searchDebounce(query)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        binding.searchEditText.addTextChangedListener(textWatcher)
    }

    private fun renderState(state: SearchState) {
        // Сначала скрываем всё, чтобы не было наложений
        with(binding) {
            recyclerView.visibility = View.GONE
            historyContainer.visibility = View.GONE
            progressBar.visibility = View.GONE
            imageError.visibility = View.GONE
            textError.visibility = View.GONE
            textExplanationError.visibility = View.GONE
            buttonUpdate.visibility = View.GONE
        }

        when (state) {
            is SearchState.Loading -> binding.progressBar.visibility = View.VISIBLE

            is SearchState.Content -> {
                binding.recyclerView.visibility = View.VISIBLE
                trackAdapter.updateData(state.tracks)
            }

            is SearchState.History -> {
                // Показываем историю, только если она не пустая
                if (state.tracks.isNotEmpty()) {
                    binding.historyContainer.visibility = View.VISIBLE
                    historyAdapter.updateData(state.tracks)
                }
                // Если история пустая, всё останется GONE (пустой экран под поиском)
            }

            is SearchState.ErrorNetwork -> {
                with(binding) {
                    imageError.visibility = View.VISIBLE
                    textError.visibility = View.VISIBLE
                    textExplanationError.visibility = View.VISIBLE
                    buttonUpdate.visibility = View.VISIBLE
                }
            }

            is SearchState.ErrorNotFound -> {
                binding.imageError.visibility = View.VISIBLE
                binding.textError.visibility = View.VISIBLE
            }

            is SearchState.Empty -> {
                // Ничего не делаем, всё уже скрыто. Это стартовое состояние.
            }
        }
    }

    private fun openPlayer(track: Track) {
        // Переход через Navigation Component
        findNavController().navigate(
            R.id.playerFragment,
            bundleOf("track" to track)
        )
    }

    private fun hideKeyboard() {
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        inputMethodManager?.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.searchEditText.removeTextChangedListener(textWatcher)
        _binding = null
    }
}