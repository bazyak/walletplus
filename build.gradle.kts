// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
}

// ---------------------------------------------------------------------------------------------
// Versioning
//
// Computed once here and shared with :app and :wear so both modules always carry the same
// version — the Data Layer refuses to pair a phone and a watch built from different versions.
//
// The patch number is bumped only when something is actually being built. A plain sync, a lint
// run or a test run leaves it alone, so the number tracks builds rather than IDE activity.
// ---------------------------------------------------------------------------------------------

val versionFile = rootProject.file("version.properties")
val versionProps = java.util.Properties().apply {
    if (versionFile.exists()) versionFile.inputStream().use { load(it) }
}

val isBuilding = gradle.startParameter.taskNames.any { taskName ->
    val task = taskName.substringAfterLast(':').lowercase()
    task.startsWith("assemble") || task.startsWith("bundle") || task.startsWith("install")
}

val bump = if (isBuilding) 1 else 0
val versionMajor = versionProps.getProperty("VERSION_MAJOR")?.toIntOrNull() ?: 1
val versionMinor = versionProps.getProperty("VERSION_MINOR")?.toIntOrNull() ?: 0
val versionPatch = (versionProps.getProperty("VERSION_PATCH")?.toIntOrNull() ?: 0) + bump
val appVersionCode = (versionProps.getProperty("VERSION_CODE")?.toIntOrNull() ?: 1) + bump
val wearVersionCode = (versionProps.getProperty("WEAR_VERSION_CODE")?.toIntOrNull() ?: 1001) + bump

if (isBuilding) {
    versionProps.setProperty("VERSION_MAJOR", versionMajor.toString())
    versionProps.setProperty("VERSION_MINOR", versionMinor.toString())
    versionProps.setProperty("VERSION_PATCH", versionPatch.toString())
    versionProps.setProperty("VERSION_CODE", appVersionCode.toString())
    versionProps.setProperty("WEAR_VERSION_CODE", wearVersionCode.toString())
    versionFile.outputStream().use { versionProps.store(it, "Updated by the build") }
}

extra["appVersionName"] = "$versionMajor.$versionMinor.$versionPatch"
extra["appVersionCode"] = appVersionCode
extra["wearVersionCode"] = wearVersionCode

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        autoCorrect = false
    }
}