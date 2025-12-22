package org.segn1s.playlistmaker.presentation.media

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.segn1s.playlistmaker.R
import org.segn1s.playlistmaker.databinding.ActivityMediaBinding

class MediaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMediaBinding
    private lateinit var tabMediator: TabLayoutMediator

    private val viewModel: MediaViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Кнопка назад
        binding.backButton.setOnClickListener {
            finish()
        }

        // 2. Настройка ViewPager2 и Адаптера
        binding.viewPager.adapter = MediaViewPagerAdapter(this)

        // 3. Настройка TabLayoutMediator
        tabMediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when(position) {
                0 -> tab.text = getString(R.string.favs)
                1 -> tab.text = getString(R.string.playlists)
            }
        }
        tabMediator.attach()

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = if (position == 0) getString(R.string.favs) else getString(R.string.playlists)
        }.attach()
    }

    override fun onDestroy() {
        super.onDestroy()
        tabMediator.detach()
    }
}