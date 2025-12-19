package org.segn1s.playlistmaker.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import org.segn1s.playlistmaker.presentation.MediaActivity
import org.segn1s.playlistmaker.R
import org.segn1s.playlistmaker.presentation.SearchActivity
import org.segn1s.playlistmaker.presentation.SettingsActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchButton = findViewById<Button>(R.id.searchButton)
        val mediaButton = findViewById<Button>(R.id.mediaButton)
        val settingsButton = findViewById<Button>(R.id.settingsButton)

        searchButton.setOnClickListener {
            val intent = Intent(this@MainActivity, SearchActivity::class.java)
            startActivity(intent)
        }

        mediaButton.setOnClickListener {
            val intent = Intent(this@MainActivity, MediaActivity::class.java)
            startActivity(intent)
        }

        settingsButton.setOnClickListener {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(intent)
        }


    }
}