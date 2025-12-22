package org.segn1s.playlistmaker.presentation.media

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.segn1s.playlistmaker.presentation.media.favs.FavoriteTracksFragment
import org.segn1s.playlistmaker.presentation.media.playlists.PlaylistsFragment

class MediaViewPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FavoriteTracksFragment.newInstance()
            else -> PlaylistsFragment.newInstance()
        }
    }
}