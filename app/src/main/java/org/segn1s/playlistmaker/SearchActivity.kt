package org.segn1s.playlistmaker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
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

    companion object {
        private const val SEARCH_TEXT_KEY = "SEARCH_TEXT_KEY"
    }

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

        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
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

        // TextWatcher с заглушкой
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                imgError.visibility= View.GONE
                txtError.visibility = View.GONE
                txtExplanationError.visibility = View.GONE
                btnUpdate.visibility = View.GONE
                clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                searchText = s?.toString() ?: ""
                // Заглушка для будущей логики поиска
            }
            override fun afterTextChanged(s: Editable?) {}
        })

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
            // Скрыть клавиатуру
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
            searchEditText.clearFocus()
        }

        // Кнопка назад
        backButton.setOnClickListener {
            finish()
        }
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

        imgError.visibility = View.VISIBLE
        imgError.setImageResource(R.drawable.placeholder_not_found)

        txtError.text = getString(R.string.not_found)
        txtError.visibility = View.VISIBLE
    }

    fun showPlaceholderNetworkError(){
        recyclerView.visibility = View.GONE

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