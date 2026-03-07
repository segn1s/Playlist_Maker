package org.segn1s.playlistmaker.presentation.playlist_with_tracks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import org.segn1s.playlistmaker.R
import org.segn1s.playlistmaker.databinding.ItemTrackInPlaylistBinding
import org.segn1s.playlistmaker.domain.model.Track

class TracksInPlaylistAdapter(
    private val onTrackClick: (Track) -> Unit,
    private val onTrackLongClick: (Track) -> Unit
) : RecyclerView.Adapter<TracksInPlaylistAdapter.ViewHolder>() {

    private val items = mutableListOf<Track>()

    fun setItems(list: List<Track>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTrackInPlaylistBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size

    inner class ViewHolder(
        private val binding: ItemTrackInPlaylistBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(track: Track) {
            binding.trackName.text = track.trackName
            binding.trackArtistAndTime.text = "${track.artistName} • ${track.trackTime}"

            Glide.with(binding.root)
                .load(track.artworkUrl100)
                .apply(RequestOptions().transform(MultiTransformation(CenterCrop(), RoundedCorners(4))))
                .placeholder(R.drawable.placeholder)
                .into(binding.trackCover)

            binding.root.setOnClickListener { onTrackClick(track) }
            binding.root.setOnLongClickListener {
                onTrackLongClick(track)
                true
            }
        }
    }
}