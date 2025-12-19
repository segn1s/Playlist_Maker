package org.segn1s.playlistmaker.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import org.segn1s.playlistmaker.R
import org.segn1s.playlistmaker.Creator
import org.segn1s.playlistmaker.domain.api.SettingsInteractor

class SettingsActivity : AppCompatActivity() {

    // 1. Объявляем Интерактор
    private lateinit var settingsInteractor: SettingsInteractor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Инициализация Интерактора через Creator
        settingsInteractor = Creator.provideSettingsInteractor(applicationContext)

        // Кнопка назад
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }

        val themeSwitcher = findViewById<SwitchMaterial>(R.id.themeSwitcher)

        // 2. Устанавливаем начальное значение, используя Интерактор (Domain Layer)
        themeSwitcher.isChecked = settingsInteractor.getDarkThemeState()

        themeSwitcher.setOnCheckedChangeListener { _, checked ->
            // 3. Переключаем тему, используя Интерактор (Domain Layer)
            // Интерактор сам сохранит состояние в SharedPreferences и вызовет App::applyTheme
            settingsInteractor.switchTheme(checked)
        }

        // --- Внешнее взаимодействие (без Интерактора) ---

        // Поделиться приложением
        findViewById<FrameLayout>(R.id.shareAppContainer).setOnClickListener {
            val shareMessage = getString(R.string.share_message, getString(R.string.android_course_url))
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(intent, getString(R.string.share_app)))
        }

        // Написать в поддержку
        findViewById<FrameLayout>(R.id.supportContainer).setOnClickListener {
            val email = getString(R.string.support_email)
            val subject = getString(R.string.support_subject)
            val body = getString(R.string.support_body)
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }
            startActivity(intent)
        }

        // Пользовательское соглашение
        findViewById<FrameLayout>(R.id.userAgreementContainer).setOnClickListener {
            val url = getString(R.string.user_agreement_url)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
    }
}