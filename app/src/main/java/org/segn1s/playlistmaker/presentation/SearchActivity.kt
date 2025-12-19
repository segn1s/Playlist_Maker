package org.segn1s.playlistmaker.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.segn1s.playlistmaker.R
import org.segn1s.playlistmaker.Creator
import org.segn1s.playlistmaker.domain.api.SearchTrackInteractor
import org.segn1s.playlistmaker.domain.api.HistoryInteractor
import org.segn1s.playlistmaker.domain.model.Track
import org.segn1s.playlistmaker.presentation.TrackAdapter
import org.segn1s.playlistmaker.presentation.PlayerActivity

class SearchActivity : AppCompatActivity() {

    // --- ENUM для состояний UI (Presentation Model) ---
    private sealed interface SearchState {
        object Loading : SearchState
        object Content : SearchState
        object ErrorNetwork : SearchState
        object ErrorNotFound : SearchState
        data class History(val tracks: List<Track>) : SearchState
        object Empty : SearchState
    }

    // --- Интеракторы (Domain Access) ---
    // ❗ Прямое взаимодействие только с Domain-слоем
    private lateinit var searchInteractor: SearchTrackInteractor
    private lateinit var historyInteractor: HistoryInteractor

    // --- UI Components ---
    private lateinit var imgError: ImageView
    private lateinit var txtError: TextView
    private lateinit var txtExplanationError: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnUpdate: Button
    private lateinit var searchEditText: EditText
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyTitle: TextView
    private lateinit var clearHistoryButton: Button
    private lateinit var historyContainer: View
    private lateinit var progressBar: ProgressBar

    // --- Debounce & Lifecycle ---
    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable { performSearch(searchEditText.text.toString().trim()) }
    private var isClickAllowed = true
    private var lastQuery: String = "" // Для кнопки "Обновить"

    companion object {
        private const val SEARCH_TEXT_KEY = "SEARCH_TEXT_KEY"
        private const val SEARCH_DEBOUNCE_DELAY_MILLIS = 2000L
        private const val CLICK_DEBOUNCE_DELAY_MILLIS = 1000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // ❗ Инициализация Интеракторов через Creator
        searchInteractor = Creator.provideSearchTrackInteractor()
        historyInteractor = Creator.provideHistoryInteractor(applicationContext)

        // --- Инициализация View (основано на вашем исходном коде) ---
        searchEditText = findViewById(R.id.searchEditText)
        val clearButton = findViewById<ImageView>(R.id.clearButton)
        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }
        imgError = findViewById(R.id.imageError)
        txtError = findViewById(R.id.textError)
        txtExplanationError = findViewById(R.id.textExplanationError)
        btnUpdate = findViewById(R.id.buttonUpdate)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        historyTitle = findViewById(R.id.historyTitle)
        clearHistoryButton = findViewById(R.id.clearHistoryButton)
        historyContainer = findViewById(R.id.historyContainer)
        progressBar = findViewById(R.id.progressBar)
        recyclerView = findViewById(R.id.recyclerView)

        // --- Адаптеры и RecyclerView ---
        trackAdapter = TrackAdapter()
        recyclerView.adapter = trackAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        historyAdapter = TrackAdapter()
        historyRecyclerView.adapter = historyAdapter
        historyRecyclerView.layoutManager = LinearLayoutManager(this)

        // --- Обработчики Кликов ---

        // Клик по результатам поиска
        trackAdapter.setOnItemClickListener { track ->
            if (clickDebounce()) {
                historyInteractor.addTrack(track) // ❗ Domain
                startPlayerActivity(track)
            }
        }

        // Клик по истории
        historyAdapter.setOnItemClickListener { track ->
            if (clickDebounce()) {
                historyInteractor.addTrack(track) // ❗ Domain
                startPlayerActivity(track)
            }
        }

        clearHistoryButton.setOnClickListener {
            historyInteractor.clearHistory() // ❗ Domain
            renderState(SearchState.Empty)
        }

        btnUpdate.setOnClickListener {
            // Повторяем последний запрос
            performSearch(lastQuery)
        }

        // --- TextWatcher ---

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""

                // Управление видимостью кнопки очистки
                clearButton.visibility = if (query.isEmpty()) View.GONE else View.VISIBLE

                if (query.isEmpty()) {
                    handler.removeCallbacks(searchRunnable)
                    // Показываем историю, если поле пустое
                    renderState(SearchState.History(historyInteractor.getHistory())) // ❗ Domain
                } else {
                    renderState(SearchState.Empty) // Скрываем историю
                    searchDebounce()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Фокус: показать историю, если нет текста
        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && searchEditText.text.isEmpty()) {
                renderState(SearchState.History(historyInteractor.getHistory())) // ❗ Domain
            } else if (!hasFocus && searchEditText.text.isEmpty()){
                renderState(SearchState.Empty) // Скрываем историю при потере фокуса, если нет текста
            }
        }

        // Изначальное состояние при запуске
        if (savedInstanceState == null || savedInstanceState.getString(SEARCH_TEXT_KEY, "").isEmpty()) {
            renderState(SearchState.History(historyInteractor.getHistory())) // ❗ Domain
        }
    }

    // --- Business Logic Access (Domain Calls) ---

    private fun performSearch(query: String) {
        if (query.isEmpty()) return
        lastQuery = query

        // Скрытие клавиатуры
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)

        renderState(SearchState.Loading)

        // ❗ Вызов Интерактора для асинхронного поиска
        searchInteractor.searchTracks(query, object : SearchTrackInteractor.TracksConsumer {

            // ❗ Обработка ответа в главном потоке для обновления UI
            override fun consume(foundTracks: List<Track>, isFailed: Boolean) {
                runOnUiThread {
                    if (isFailed) {
                        renderState(SearchState.ErrorNetwork)
                    } else if (foundTracks.isEmpty()) {
                        renderState(SearchState.ErrorNotFound)
                    } else {
                        trackAdapter.updateData(foundTracks)
                        renderState(SearchState.Content)
                    }
                }
            }
        })
    }

    // --- UI Management (Render State) ---

    private fun renderState(state: SearchState) {
        // Скрываем все элементы
        recyclerView.visibility = View.GONE
        historyContainer.visibility = View.GONE
        historyTitle.visibility = View.GONE
        historyRecyclerView.visibility = View.GONE
        clearHistoryButton.visibility = View.GONE
        progressBar.visibility = View.GONE
        imgError.visibility = View.GONE
        txtError.visibility = View.GONE
        txtExplanationError.visibility = View.GONE
        btnUpdate.visibility = View.GONE

        when (state) {
            SearchState.Loading -> {
                progressBar.visibility = View.VISIBLE
            }
            SearchState.Content -> {
                recyclerView.visibility = View.VISIBLE
            }
            SearchState.ErrorNotFound -> {
                imgError.setImageResource(R.drawable.placeholder_not_found)
                imgError.visibility = View.VISIBLE
                txtError.text = getString(R.string.not_found)
                txtError.visibility = View.VISIBLE
            }
            SearchState.ErrorNetwork -> {
                imgError.setImageResource(R.drawable.placeholder_network_error)
                imgError.visibility = View.VISIBLE
                txtError.text = getString(R.string.network_error)
                txtError.visibility = View.VISIBLE
                txtExplanationError.text = getString(R.string.explanation_network_error)
                txtExplanationError.visibility = View.VISIBLE
                btnUpdate.visibility = View.VISIBLE
            }
            is SearchState.History -> {
                // Показываем историю только если она не пуста
                if (state.tracks.isNotEmpty()) {
                    historyAdapter.updateData(state.tracks.take(10))
                    historyContainer.visibility = View.VISIBLE
                    historyTitle.visibility = View.VISIBLE
                    historyRecyclerView.visibility = View.VISIBLE
                    clearHistoryButton.visibility = View.VISIBLE
                }
            }
            SearchState.Empty -> {
                // Элементы скрыты по умолчанию
            }
        }
    }

    // --- Helper Methods ---

    private fun startPlayerActivity(track: Track) {
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra("track_extra", track)
        }
        startActivity(intent)
    }

    private fun clickDebounce(): Boolean {
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY_MILLIS)
            return true
        }
        return false
    }

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY_MILLIS)
    }

    // --- Lifecycle Methods ---

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(searchRunnable)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT_KEY, searchEditText.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val restoredText = savedInstanceState.getString(SEARCH_TEXT_KEY, "")
        searchEditText.setText(restoredText)
        if (restoredText.isNotEmpty()) {
            // Если текст был восстановлен, запускаем поиск
            performSearch(restoredText)
        } else {
            // Иначе показываем историю
            renderState(SearchState.History(historyInteractor.getHistory()))
        }
    }
}