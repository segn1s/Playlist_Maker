package org.segn1s.playlistmaker.presentation.media.playlists.creating

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.segn1s.playlistmaker.R
import org.segn1s.playlistmaker.databinding.ItemPlaylistBottomSheetBinding
import org.segn1s.playlistmaker.domain.model.Playlist
import java.io.File

class BottomSheetPlaylistAdapter(
    private val onPlaylistClick: (Playlist) -> Unit
) : RecyclerView.Adapter<BottomSheetPlaylistAdapter.ViewHolder>() {

    private val items = mutableListOf<Playlist>()

    fun setItems(list: List<Playlist>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPlaylistBottomSheetBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size

    inner class ViewHolder(
        private val binding: ItemPlaylistBottomSheetBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist) {
            binding.playlistName.text = playlist.name
            binding.playlistTracksCount.text = playlist.tracksCount.toTracksString()

            val coverFile = playlist.coverPath?.let { File(it) }
            Glide.with(binding.root)
                .load(if (coverFile?.exists() == true) coverFile else null)
                .placeholder(R.drawable.placeholder)
                .centerCrop()
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