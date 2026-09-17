package com.bazyak.walletplus.education

import android.content.Context

class SharedPreferencesEducationProgressRepository(context: Context) : EducationProgressRepository {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun isOnboardingCompleted(): Boolean = preferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)

    override fun setOnboardingCompleted() {
        preferences.edit()
            .putBoolean(KEY_ONBOARDING_COMPLETED, true)
            .apply()
    }

    override fun isEducationCompleted(educationId: String): Boolean = educationId in completedEducationIds()

    override fun setEducationCompleted(educationId: String) {
        val updated = completedEducationIds() + educationId
        preferences.edit()
            .putStringSet(KEY_COMPLETED_EDUCATIONS, updated)
            .apply()
    }

    private fun completedEducationIds(): Set<String> =
        preferences.getStringSet(KEY_COMPLETED_EDUCATIONS, emptySet()).orEmpty().toSet()

    private companion object {
        const val PREFERENCES_NAME = "education_progress"
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        const val KEY_COMPLETED_EDUCATIONS = "completed_educations"
    }
}
