package org.segn1s.playlistmaker.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.segn1s.playlistmaker.R
import org.segn1s.playlistmaker.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Инициализация Binding
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()
    }

    private fun observeViewModel() {
        // Подписываемся на изменение темы
        viewModel.themeSettingsState.observe(this) { isDark ->
            binding.themeSwitcher.isChecked = isDark
        }
    }

    private fun setupListeners() {
        // Назад
        binding.backButton.setOnClickListener { finish() }

        // Переключатель темы
        binding.themeSwitcher.setOnCheckedChangeListener { _, checked ->
            viewModel.switchTheme(checked)
        }

        // Поделиться (Внешняя логика)
        binding.shareAppContainer.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message, getString(R.string.android_course_url)))
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app)))
        }

        // Поддержка
        binding.supportContainer.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.support_email)))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_subject))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.support_body))
            }
            startActivity(intent)
        }

        // Соглашение
        binding.userAgreementContainer.setOnClickListener {
            val intent =
                Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.user_agreement_url)))
            startActivity(intent)
        }
    }
}