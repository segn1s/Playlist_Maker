package org.segn1s.playlistmaker.presentation.media.playlists

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import org.segn1s.playlistmaker.R
import org.segn1s.playlistmaker.databinding.ItemPlaylistBinding
import org.segn1s.playlistmaker.domain.model.Playlist
import java.io.File

class PlaylistAdapter(
    private val onPlaylistClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    private val items = mutableListOf<Playlist>()

    fun setItems(list: List<Playlist>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemPlaylistBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class PlaylistViewHolder(
        private val binding: ItemPlaylistBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist) {
            binding.playlistName.text = playlist.name
            binding.playlistTracksCount.text = playlist.tracksCount.toTracksString()

            val coverFile = playlist.coverPath?.let { File(it) }
            Glide.with(binding.root)
                .load(if (coverFile?.exists() == true) coverFile else null)
                .apply(RequestOptions().transform(MultiTransformation(CenterCrop(), RoundedCorners(16))))
                .placeholder(R.drawable.placeholder)
                .into(binding.playlistCover)

            binding.root.setOnClickListener { onPlaylistClick(playlist) }
        }

        private fun Int.toTracksString(): String {
            val lastTwo = this % 100
            val lastOne = this % 10
            return when {
                lastTwo in 11..19 -> "$this треков"
                lastOne == 1 -> "$this трек"
                lastOne in 2..4 -> "$this трека"
                else -> "$this треков"
            }
        }
    }
}