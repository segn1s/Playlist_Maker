package org.segn1s.playlistmaker.presentation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.segn1s.playlistmaker.databinding.ActivityMainBinding
import org.segn1s.playlistmaker.presentation.media.MediaActivity
import org.segn1s.playlistmaker.presentation.search.SearchActivity
import org.segn1s.playlistmaker.presentation.settings.SettingsActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.searchButton.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.mediaButton.setOnClickListener {
            startActivity(Intent(this, MediaActivity::class.java))
        }
    }
}