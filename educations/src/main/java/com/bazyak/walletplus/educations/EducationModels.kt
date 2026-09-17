package com.bazyak.walletplus.educations

import androidx.compose.runtime.Immutable

@Immutable
data class Education(val id: String, val steps: List<EducationStep>)

@Immutable
data class ActiveEducation(val education: Education, val stepIndex: Int) {
    val step: EducationStep
        get() = education.steps[stepIndex]

    val canGoBack: Boolean
        get() = stepIndex > 0

    val isLastStep: Boolean
        get() = stepIndex == education.steps.lastIndex
}

@Immutable
data class EducationStep(
    val id: String,
    val text: String,
    val targetKey: String? = null,
    val title: String? = null,
    val bullets: List<String> = emptyList(),
    val placement: EducationStepPlacement = if (targetKey == null) {
        EducationStepPlacement.Center
    } else {
        EducationStepPlacement.NearTarget
    },
    val illustration: EducationIllustration? = null,
)

enum class EducationStepPlacement {
    NearTarget,
    Center,
}

enum class EducationIllustration {
    PullToRefresh,
}
