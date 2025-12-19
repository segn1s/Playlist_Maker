package org.segn1s.playlistmaker.presentation.common

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.segn1s.playlistmaker.R
import org.segn1s.playlistmaker.domain.model.Track

class TrackAdapter : RecyclerView.Adapter<TrackViewHolder>() {

    private val tracks = mutableListOf<Track>()
    private var onItemClickListener: ((Track) -> Unit)? = null
    private val maxItems = 10

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.bind(track)
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(track)
            Log.d("Track added", "Track name: ${track.trackName}")
        }
    }

    override fun getItemCount(): Int = tracks.size

    fun updateData(newTracks: List<Track>) {
        tracks.clear()
        tracks.addAll(newTracks)
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: (Track) -> Unit) {
        onItemClickListener = listener
    }
}