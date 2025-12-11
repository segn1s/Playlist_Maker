package org.segn1s.playlistmaker.presentation

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import org.segn1s.playlistmaker.R
import org.segn1s.playlistmaker.domain.model.Track

class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val ivArtwork: ImageView = itemView.findViewById(R.id.ivArtwork)
    private val tvTrackName: TextView = itemView.findViewById(R.id.tvTrackName)
    private val tvArtistAndTime: TextView = itemView.findViewById(R.id.tvArtistAndTime)

    fun bind(track: Track) {
        tvTrackName.text = track.trackName
        tvArtistAndTime.text = "${track.artistName} • ${track.trackTime}"

        Glide.with(itemView.context)
            .load(track.artworkUrl100)
            .transform(RoundedCorners(12))
            .placeholder(R.drawable.placeholder)
            .into(ivArtwork)
    }
}