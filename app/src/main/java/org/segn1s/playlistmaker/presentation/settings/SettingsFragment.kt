package org.segn1s.playlistmaker.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.segn1s.playlistmaker.R
import org.segn1s.playlistmaker.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeViewModel()
    }

    private fun observeViewModel() {
        // Используем viewLifecycleOwner вместо this
        viewModel.themeSettingsState.observe(viewLifecycleOwner) { isDark ->
            binding.themeSwitcher.isChecked = isDark
        }
    }

    private fun setupListeners() {

        // Переключатель темы
        binding.themeSwitcher.setOnCheckedChangeListener { _, checked ->
            viewModel.switchTheme(checked)
        }

        // Поделиться
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
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.user_agreement_url)))
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Важно для предотвращения утечек памяти
    }
}