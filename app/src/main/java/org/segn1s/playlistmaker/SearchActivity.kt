package org.segn1s.playlistmaker

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.os.Handler
import android.os.Looper

class SearchActivity : AppCompatActivity() {
    private var searchText: String = ""
    private lateinit var imgError: ImageView
    private lateinit var txtError: TextView
    private lateinit var txtExplanationError: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnUpdate: Button
    private lateinit var searchEditText: EditText
    private lateinit var api: ITunesApi
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var searchHistory: SearchHistory
    private lateinit var historyAdapter: TrackAdapter
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyTitle: TextView
    private lateinit var clearHistoryButton: Button
    private lateinit var historyContainer: View
    private lateinit var progressBar: ProgressBar // Добавлено свойство ProgressBar

    // Debounce свойства
    private val handler = Handler(Looper.getMainLooper())
    // Runnable для выполнения поискового запроса
    private val searchRunnable = Runnable {
        val text = searchEditText.text.toString().trim()
        if (text.isNotEmpty()) {
            performSearch(text)
        }
    }
    private var isClickAllowed = true // Флаг для debounce кликов

    companion object {
        private const val SEARCH_TEXT_KEY = "SEARCH_TEXT_KEY"
        private const val SEARCH_DEBOUNCE_DELAY_MILLIS = 2000L // 2 секунды
        private const val CLICK_DEBOUNCE_DELAY_MILLIS = 1000L // 1 секунда
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        searchEditText = findViewById(R.id.searchEditText)
        val clearButton = findViewById<ImageView>(R.id.clearButton)
        val backButton = findViewById<ImageView>(R.id.backButton)
        imgError = findViewById(R.id.imageError)
        txtError = findViewById(R.id.textError)
        txtExplanationError = findViewById(R.id.textExplanationError)
        btnUpdate = findViewById(R.id.buttonUpdate)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        historyTitle = findViewById(R.id.historyTitle)
        clearHistoryButton = findViewById(R.id.clearHistoryButton)
        historyContainer = findViewById(R.id.historyContainer)
        progressBar = findViewById(R.id.progressBar) // Инициализация ProgressBar

        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }

        val prefs = getSharedPreferences("playlist_maker_prefs", MODE_PRIVATE)
        searchHistory = SearchHistory(prefs)
        historyAdapter = TrackAdapter()

        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter

        // Клик по истории с Debounce
        historyAdapter.setOnItemClickListener { track ->
            if (clickDebounce()) {
                searchHistory.addTrack(track)
                updateHistoryView()
                startActivity(
                    android.content.Intent(this, PlayerActivity::class.java).apply {
                        putExtra("track_extra", track)
                    }
                )
            }
        }

        clearHistoryButton.setOnClickListener {
            searchHistory.clearHistory()
            updateHistoryView()
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(ITunesApi::class.java)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        trackAdapter = TrackAdapter()
        recyclerView.adapter = trackAdapter

        // Клик по результатам поиска с Debounce
        trackAdapter.setOnItemClickListener { track ->
            if (clickDebounce()) {
                searchHistory.addTrack(track)
                updateHistoryView()
                startActivity(
                    android.content.Intent(this, PlayerActivity::class.java).apply {
                        putExtra("track_extra", track)
                    }
                )
            }
        }

        // TextWatcher с debounce для поиска
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchText = s?.toString() ?: ""
                val isEmpty = searchText.isEmpty()

                // Управление видимостью истории и debounce
                if (isEmpty) {
                    handler.removeCallbacks(searchRunnable) // Отмена ожидающего поиска
                    recyclerView.visibility = View.GONE
                    updateHistoryView() // Показ истории
                } else {
                    hideHistory()
                    searchDebounce() // Запуск/сброс таймера debounce
                }

                // Скрытие плейсхолдеров
                hidePlaceholders()

                clearButton.visibility = if (isEmpty) View.GONE else View.VISIBLE
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Удаляем вызов performSearch из IME_ACTION_DONE, теперь поиск управляется debounce
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Если пользователь нажал Done, мы можем просто скрыть клавиатуру,
                // debounce запустит поиск, если не было дальнейшего ввода.
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
            }
            false
        }

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && searchEditText.text.isNotEmpty()) {
                hideHistory()
            } else {
                updateHistoryView()
            }
        }

        // Кнопка очистки
        clearButton.setOnClickListener {
            handler.removeCallbacks(searchRunnable) // Отмена текущего поиска
            searchEditText.text.clear()
            clearButton.visibility = View.GONE
            trackAdapter.updateData(emptyList())
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
            searchEditText.clearFocus()
            updateHistoryView()
        }

        // Кнопка назад
        backButton.setOnClickListener {
            finish()
        }

        updateHistoryView()
    }

    // --- Debounce Methods ---

    /** Запускает или перезапускает таймер поиска. */
    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY_MILLIS)
    }

    /** Проверяет, разрешен ли клик, и устанавливает задержку. */
    private fun clickDebounce(): Boolean {
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY_MILLIS)
            return true
        }
        return false
    }

    // --- Search Logic & UI Management ---

    private fun performSearch(query: String) {
        if (query.isEmpty()) return

        showLoading() // Показать индикатор загрузки

        api.searchSongs(query).enqueue(object : Callback<SearchResponse> {
            override fun onResponse(
                call: Call<SearchResponse>,
                response: Response<SearchResponse>
            ) {
                hideLoading() // Скрыть индикатор загрузки при получении ответа

                if (response.isSuccessful && response.body() != null) {
                    val results = response.body()!!.results
                    if (results.isEmpty()) {
                        showPlaceholderNotFound()
                    } else {
                        showResults(results)
                    }
                } else {
                    showPlaceholderNetworkError()
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                hideLoading() // Скрыть индикатор загрузки при ошибке
                showPlaceholderNetworkError()
            }
        })
    }

    // --- Placeholder Methods ---

    private fun hidePlaceholders() {
        imgError.visibility = View.GONE
        txtError.visibility = View.GONE
        txtExplanationError.visibility = View.GONE
        btnUpdate.visibility = View.GONE
    }

    private fun showLoading() {
        recyclerView.visibility = View.GONE
        hideHistory()
        hidePlaceholders()
        progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        progressBar.visibility = View.GONE
    }

    fun showPlaceholderNotFound(){
        hideLoading()
        recyclerView.visibility = View.GONE
        hideHistory()

        imgError.visibility = View.VISIBLE
        imgError.setImageResource(R.drawable.placeholder_not_found)

        txtError.text = getString(R.string.not_found)
        txtError.visibility = View.VISIBLE
    }

    fun showPlaceholderNetworkError(){
        hideLoading()
        recyclerView.visibility = View.GONE
        hideHistory()

        imgError.visibility = View.VISIBLE
        imgError.setImageResource(R.drawable.placeholder_network_error)

        txtError.text = getString(R.string.network_error)
        txtError.visibility = View.VISIBLE

        txtExplanationError.text = getString(R.string.explanation_network_error)
        txtExplanationError.visibility = View.VISIBLE

        btnUpdate.visibility = View.VISIBLE
        btnUpdate.setOnClickListener {
            hidePlaceholders()
            performSearch(searchEditText.text.toString().trim())
        }
    }

    private fun showResults(results: List<Track>) {
        hideLoading()
        recyclerView.visibility = View.VISIBLE
        trackAdapter.updateData(results)
        hideHistory()
    }

    private fun updateHistoryView() {
        val history = searchHistory.getHistory()
        if (searchEditText.text.isEmpty() && history.isNotEmpty()) {
            historyContainer.visibility = View.VISIBLE
            historyTitle.visibility = View.VISIBLE
            historyRecyclerView.visibility = View.VISIBLE
            clearHistoryButton.visibility = View.VISIBLE

            historyAdapter.updateData(history.take(10))

            hidePlaceholders()
        } else {
            hideHistory()
        }
    }

    private fun hideHistory() {
        findViewById<View>(R.id.historyContainer).visibility = View.GONE
        historyTitle.visibility = View.GONE
        historyRecyclerView.visibility = View.GONE
        clearHistoryButton.visibility = View.GONE
    }

    // --- Lifecycle Methods ---

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT_KEY, searchText)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val restoredText = savedInstanceState.getString(SEARCH_TEXT_KEY, "")
        findViewById<EditText>(R.id.searchEditText).setText(restoredText)
    }
}