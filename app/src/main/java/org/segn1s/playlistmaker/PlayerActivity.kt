package org.segn1s.playlistmaker

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide

class PlayerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val track = intent.getSerializableExtra("track_extra") as? Track

        // Views
        val backButton = findViewById<ImageButton>(R.id.back_button)
        val albumCover = findViewById<ImageView>(R.id.album_cover)
        val trackName = findViewById<TextView>(R.id.track_name)
        val artistName = findViewById<TextView>(R.id.artist_name)
        val textViewDurationLabel = findViewById<TextView>(R.id.track_duration)
        val textViewAlbumLabel = findViewById<TextView>(R.id.album_info)
        val textViewYearLabel = findViewById<TextView>(R.id.release_year)
        val textViewGenreLabel = findViewById<TextView>(R.id.genre_info)
        val textViewCountryLabel = findViewById<TextView>(R.id.country_info)

        val tvDuration = findViewById<TextView>(R.id.track_duration_data)
        val tvAlbum = findViewById<TextView>(R.id.album_info_data)
        val tvYear = findViewById<TextView>(R.id.release_year_data)
        val tvGenre = findViewById<TextView>(R.id.genre_info_data)
        val tvCountry = findViewById<TextView>(R.id.country_info_data)

        backButton.setOnClickListener { finish() }

        track?.let {
            trackName.text = it.trackName
            artistName.text = it.artistName

            tvDuration.text = it.trackTime ?: "--:--"
            tvAlbum.text = it.collectionName ?: getString(R.string.album)
            // releaseDate может быть в ISO, выводим только год
            tvYear.text = it.releaseDate?.take(4) ?: getString(R.string.year)
            tvGenre.text = it.primaryGenreName ?: getString(R.string.genre)
            tvCountry.text = it.country ?: getString(R.string.country)

            val highResUrl = it.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")

            Glide.with(this)
                .load(highResUrl)
                .placeholder(R.drawable.placeholder)
                .into(albumCover)
        }
    }
}