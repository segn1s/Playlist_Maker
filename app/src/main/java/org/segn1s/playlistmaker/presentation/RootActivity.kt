package org.segn1s.playlistmaker.presentation

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI // Добавляем этот импорт
import androidx.navigation.ui.setupWithNavController
import org.segn1s.playlistmaker.R
import org.segn1s.playlistmaker.databinding.ActivityRootBinding

class RootActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRootBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRootBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBar.top)
            insets
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.rootFragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavigationView = binding.bottomNavigationView // Используем binding, раз он есть

        // 1. Стандартная связка (чтобы работало переключение и выделение иконок)
        bottomNavigationView.setupWithNavController(navController)

        bottomNavigationView.setOnItemSelectedListener { item ->
            // Это заставляет навигацию переключать фрагменты правильно
            NavigationUI.onNavDestinationSelected(item, navController)
            return@setOnItemSelectedListener true
        }

        // 3. Управляем видимостью BottomNav (Плеер на весь экран)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.playerFragment -> {
                    bottomNavigationView.visibility = View.GONE
                }
                R.id.createPlaylistFragment -> {
                    bottomNavigationView.visibility = View.GONE
                }
                R.id.playlistWithTracksFragment -> {
                    bottomNavigationView.visibility = View.GONE
                }
                R.id.editPlaylistFragment -> {
                    bottomNavigationView.visibility = View.GONE
                }
                else -> {
                    bottomNavigationView.visibility = View.VISIBLE
                }
            }
        }
    }
}