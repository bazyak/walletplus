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

val buildTasks = gradle.startParameter.taskNames.filter { taskName ->
    val task = taskName.substringAfterLast(':').lowercase()
    task.startsWith("assemble") || task.startsWith("bundle") || task.startsWith("install")
}

// The watch APK is built in its own Gradle invocation, and bumping again there would hand the
// watch a different version than the phone it pairs with. So only a build that includes the phone
// app moves the number; a watch-only build reuses whatever the phone was last built with.
val isWearOnlyBuild = buildTasks.isNotEmpty() && buildTasks.all { taskName ->
    taskName.removePrefix(":").startsWith("wear:")
}

val bump = if (buildTasks.isNotEmpty() && !isWearOnlyBuild) 1 else 0
val versionMajor = versionProps.getProperty("VERSION_MAJOR")?.toIntOrNull() ?: 1
val versionMinor = versionProps.getProperty("VERSION_MINOR")?.toIntOrNull() ?: 0
val versionPatch = (versionProps.getProperty("VERSION_PATCH")?.toIntOrNull() ?: 0) + bump
val sharedVersionCode = (versionProps.getProperty("VERSION_CODE")?.toIntOrNull() ?: 1) + bump

if (bump > 0) {
    versionProps.setProperty("VERSION_MAJOR", versionMajor.toString())
    versionProps.setProperty("VERSION_MINOR", versionMinor.toString())
    versionProps.setProperty("VERSION_PATCH", versionPatch.toString())
    versionProps.setProperty("VERSION_CODE", sharedVersionCode.toString())
    versionFile.outputStream().use { versionProps.store(it, "Updated by the build") }
}

// Both modules carry the same versionCode and versionName: they are two halves of one app, and
// the Data Layer will not pair a phone and a watch built from different versions.
extra["appVersionName"] = "$versionMajor.$versionMinor.$versionPatch"
extra["appVersionCode"] = sharedVersionCode

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        autoCorrect = false
    }
}