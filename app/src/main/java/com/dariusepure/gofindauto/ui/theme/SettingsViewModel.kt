package com.dariusepure.gofindauto.ui.theme

import com.dariusepure.gofindauto.data.prefs.PreferenceRepository
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
    private val authRepository: com.dariusepure.gofindauto.data.auth.AuthRepository
) : androidx.lifecycle.ViewModel() {
    
    val isDarkMode = preferenceRepository.isDarkMode
    
    fun getUserEmail(): String? = authRepository.getUserEmail()

    fun toggleTheme(currentDark: Boolean) {
        preferenceRepository.setDarkMode(!currentDark)
    }

    fun setDarkMode(enabled: Boolean?) {
        preferenceRepository.setDarkMode(enabled)
    }
}
