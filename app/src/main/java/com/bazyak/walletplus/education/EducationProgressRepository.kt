package com.bazyak.walletplus.education

interface EducationProgressRepository {
    fun isOnboardingCompleted(): Boolean

    fun setOnboardingCompleted()

    fun isEducationCompleted(educationId: String): Boolean

    fun setEducationCompleted(educationId: String)
}
