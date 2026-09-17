package com.bazyak.walletplus.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.bazyak.walletplus.education.EducationProgressRepository
import com.bazyak.walletplus.education.PassGridEducationIds
import com.bazyak.walletplus.education.createPassGridEmptyEducation
import com.bazyak.walletplus.education.createPassGridFirstCardEducation
import com.bazyak.walletplus.educations.ActiveEducation
import com.bazyak.walletplus.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EducationViewModel(
    private val progressRepository: EducationProgressRepository,
    private val settingsRepository: SettingsRepository,
    private val context: Context,
) : ViewModel() {

    private val sessionDismissedEducationIds = mutableSetOf<String>()
    private var isExternalEntry = false

    private val _activeEducation = MutableStateFlow<ActiveEducation?>(null)
    val activeEducation: StateFlow<ActiveEducation?> = _activeEducation.asStateFlow()

    fun startAppEntry(isExternalImport: Boolean) {
        isExternalEntry = isExternalImport
    }

    fun shouldShowOnboarding(isExternalImport: Boolean): Boolean {
        if (isExternalImport) return false
        return forceShowEducations || !progressRepository.isOnboardingCompleted()
    }

    fun completeOnboarding() {
        if (!forceShowEducations) {
            progressRepository.setOnboardingCompleted()
        }
    }

    fun showPassGridEducationIfNeeded(passCount: Int, isInitialLoading: Boolean) {
        if (isInitialLoading || isExternalEntry || _activeEducation.value != null) return

        val education = when {
            passCount == 0 -> createPassGridEmptyEducation(context)
            passCount > 0 -> createPassGridFirstCardEducation(context)
            else -> null
        } ?: return

        if (shouldShowEducation(education.id)) {
            _activeEducation.value = ActiveEducation(education = education, stepIndex = 0)
        }
    }

    fun nextEducationStep() {
        val active = _activeEducation.value ?: return
        if (active.isLastStep) {
            finishActiveEducation()
        } else {
            _activeEducation.value = active.copy(stepIndex = active.stepIndex + 1)
        }
    }

    fun previousEducationStep() {
        val active = _activeEducation.value ?: return
        if (active.canGoBack) {
            _activeEducation.value = active.copy(stepIndex = active.stepIndex - 1)
        }
    }

    fun finishActiveEducation() {
        val educationId = _activeEducation.value?.education?.id ?: return
        if (forceShowEducations) {
            sessionDismissedEducationIds += educationId
        } else {
            progressRepository.setEducationCompleted(educationId)
        }
        _activeEducation.value = null
    }

    private fun shouldShowEducation(educationId: String): Boolean {
        if (forceShowEducations) {
            return educationId !in sessionDismissedEducationIds
        }

        val onboardingReady = progressRepository.isOnboardingCompleted()
        if (!onboardingReady) return false

        return when (educationId) {
            PassGridEducationIds.EMPTY,
            PassGridEducationIds.FIRST_CARD,
            -> !progressRepository.isEducationCompleted(educationId)
            else -> false
        }
    }

    private val forceShowEducations: Boolean
        get() = settingsRepository.showEducations.value
}
