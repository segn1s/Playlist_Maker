package org.segn1s.playlistmaker.presentation.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import org.segn1s.playlistmaker.Creator
import org.segn1s.playlistmaker.databinding.ActivitySearchBinding
import org.segn1s.playlistmaker.domain.model.Track
import org.segn1s.playlistmaker.presentation.search.SearchViewModel
import org.segn1s.playlistmaker.presentation.common.TrackAdapter

class SearchActivity : AppCompatActivity() {

    // Весь UI теперь живет здесь
    private lateinit var binding: ActivitySearchBinding
    private lateinit var viewModel: SearchViewModel

    private val trackAdapter = TrackAdapter()
    private val historyAdapter = TrackAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация Binding
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация ViewModel
        viewModel = ViewModelProvider(this, Creator.getSearchViewModelFactory(this))
            .get(SearchViewModel::class.java)

        setupAdapters()
        setupListeners()

        // Подписка на состояние из ViewModel
        viewModel.stateLiveData.observe(this) { state ->
            renderState(state)
        }
    }

    private fun setupAdapters() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = trackAdapter

        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.historyRecyclerView.adapter = historyAdapter
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener { finish() }

        binding.clearButton.setOnClickListener {
            binding.searchEditText.text.clear()
            viewModel.showHistory()
        }

        binding.clearHistoryButton.setOnClickListener {
            viewModel.clearHistory()
        }

        binding.buttonUpdate.setOnClickListener {
            viewModel.performSearch(binding.searchEditText.text.toString())
        }

        // Адаптеры
        trackAdapter.setOnItemClickListener { track ->
            viewModel.addTrackToHistory(track)
            startPlayerActivity(track)
        }

        historyAdapter.setOnItemClickListener { track ->
            viewModel.addTrackToHistory(track)
            startPlayerActivity(track)
        }

        // Работа с текстом
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
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
        })
    }

    private fun renderState(state: SearchState) {
        // Скрываем всё через binding
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
                binding.historyContainer.visibility = View.VISIBLE
                historyAdapter.updateData(state.tracks)
            }

            is SearchState.ErrorNetwork -> {
                with(binding) {
                    imageError.visibility = View.VISIBLE
                    textError.visibility = View.VISIBLE
                    textExplanationError.visibility = View.VISIBLE
                    buttonUpdate.visibility = View.VISIBLE
                    // Здесь можно установить конкретные строки/картинки
                }
            }

            is SearchState.ErrorNotFound -> {
                binding.imageError.visibility = View.VISIBLE
                binding.textError.visibility = View.VISIBLE
            }

            is SearchState.Empty -> { /* Все скрыто */ }
        }
    }

    private fun startPlayerActivity(track: Track) {
        // Твоя логика перехода
    }
}