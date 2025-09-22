package org.segn1s.playlistmaker

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
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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

    companion object {
        private const val SEARCH_TEXT_KEY = "SEARCH_TEXT_KEY"
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        searchEditText = findViewById<EditText>(R.id.searchEditText)
        val clearButton = findViewById<ImageView>(R.id.clearButton)
        val backButton = findViewById<ImageView>(R.id.backButton)
        imgError = findViewById<ImageView>(R.id.imageError)
        txtError = findViewById<TextView>(R.id.textError)
        txtExplanationError = findViewById<TextView>(R.id.textExplanationError)
        btnUpdate = findViewById<Button>(R.id.buttonUpdate)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        historyTitle = findViewById(R.id.historyTitle)
        clearHistoryButton = findViewById(R.id.clearHistoryButton)
        historyContainer = findViewById(R.id.historyContainer)

        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }

        val prefs = getSharedPreferences("playlist_maker_prefs", MODE_PRIVATE)
        searchHistory = SearchHistory(prefs)
        historyAdapter = TrackAdapter()

        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter

        historyAdapter.setOnItemClickListener { track ->
            searchHistory.addTrack(track)
            Log.d("SearchHistory", "Track added: ${track.trackName}")
            updateHistoryView()
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

        // Добавляем клики по результатам поиска в историю
        trackAdapter.setOnItemClickListener { track ->
            searchHistory.addTrack(track)
            updateHistoryView()
        }

        // TextWatcher с корректным управлением видимостью
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isEmpty = s.isNullOrEmpty()
                if (isEmpty) {
                    // показываем историю, если есть
                    recyclerView.visibility = View.GONE
                    updateHistoryView()
                } else {
                    // прячем историю при вводе
                    hideHistory()
                }

                imgError.visibility= View.GONE
                txtError.visibility = View.GONE
                txtExplanationError.visibility = View.GONE
                btnUpdate.visibility = View.GONE
                clearButton.visibility = if (isEmpty) View.GONE else View.VISIBLE
                searchText = s?.toString() ?: ""
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && searchEditText.text.isNotEmpty()) {
                hideHistory()
            } else {
                updateHistoryView()
            }
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val text = searchEditText.text.toString().trim()
                if (text.isNotEmpty()) {
                    imgError.visibility= View.GONE
                    txtError.visibility = View.GONE
                    txtExplanationError.visibility = View.GONE
                    btnUpdate.visibility = View.GONE
                    performSearch(text)
                }
                true
            } else {
                false
            }
        }

        // Кнопка очистки
        clearButton.setOnClickListener {
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

    private fun performSearch(query: String) {
        api.searchSongs(query).enqueue(object : Callback<SearchResponse> {
            override fun onResponse(
                call: Call<SearchResponse>,
                response: Response<SearchResponse>
            ) {
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
                showPlaceholderNetworkError()
            }
        })
    }

    fun showPlaceholderNotFound(){
        recyclerView.visibility = View.GONE
        hideHistory()

        imgError.visibility = View.VISIBLE
        imgError.setImageResource(R.drawable.placeholder_not_found)

        txtError.text = getString(R.string.not_found)
        txtError.visibility = View.VISIBLE
    }

    fun showPlaceholderNetworkError(){
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
            imgError.visibility= View.GONE
            txtError.visibility = View.GONE
            txtExplanationError.visibility = View.GONE
            btnUpdate.visibility = View.GONE
            performSearch(searchEditText.text.toString().trim())
        }
    }

    private fun showResults(results: List<Track>) {
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

            imgError.visibility = View.GONE
            txtError.visibility = View.GONE
            txtExplanationError.visibility = View.GONE
            btnUpdate.visibility = View.GONE
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