package com.bazyak.walletplus.educations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned

class EducationTargetRegistry {
    private val targetBounds = mutableStateMapOf<String, Rect>()

    fun boundsFor(key: String): Rect? = targetBounds[key]

    fun update(key: String, bounds: Rect) {
        targetBounds[key] = bounds
    }

    fun remove(key: String) {
        targetBounds.remove(key)
    }
}

val LocalEducationTargetRegistry = staticCompositionLocalOf<EducationTargetRegistry?> { null }

@Composable
fun EducationTargetProvider(content: @Composable () -> Unit) {
    val registry = androidx.compose.runtime.remember { EducationTargetRegistry() }
    CompositionLocalProvider(
        LocalEducationTargetRegistry provides registry,
        content = content,
    )
}

fun Modifier.educationTarget(key: String): Modifier = composed {
    val registry = LocalEducationTargetRegistry.current
    DisposableEffect(registry, key) {
        onDispose {
            registry?.remove(key)
        }
    }

    if (registry == null) {
        this
    } else {
        onGloballyPositioned { coordinates ->
            registry.update(key, coordinates.boundsInRoot())
        }
    }
}
