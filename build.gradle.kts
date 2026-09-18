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

// A task without a module prefix (":assembleRelease") runs in every module, so it counts as both.
fun targets(module: String) = buildTasks.any { taskName ->
    val task = taskName.removePrefix(":")
    !task.contains(":") || task.startsWith("$module:")
}

val buildsApp = targets("app")
val buildsWear = targets("wear")

var appPatch = versionProps.getProperty("APP_VERSION_PATCH")?.toIntOrNull() ?: 0
var wearPatch = versionProps.getProperty("WEAR_VERSION_PATCH")?.toIntOrNull() ?: 0
var appCode = versionProps.getProperty("APP_VERSION_CODE")?.toIntOrNull() ?: 1
var wearCode = versionProps.getProperty("WEAR_VERSION_CODE")?.toIntOrNull() ?: 1

// The two modules carry their own numbers, because a change often touches only one of them and
// rebuilding the other just to keep the numbers together would be busywork.
//
// Building a module that is behind the other catches it up instead of bumping; building the one
// that is already ahead moves it forward. Equal versions therefore always bump, and a module never
// silently ships under a version that was already used by its counterpart.
when {
    buildsApp && buildsWear -> {
        val patch = maxOf(appPatch, wearPatch) + 1
        val code = maxOf(appCode, wearCode) + 1
        appPatch = patch
        wearPatch = patch
        appCode = code
        wearCode = code
    }

    buildsApp -> if (appPatch < wearPatch) {
        appPatch = wearPatch
        appCode = wearCode
    } else {
        appPatch++
        appCode++
    }

    buildsWear -> if (wearPatch < appPatch) {
        wearPatch = appPatch
        wearCode = appCode
    } else {
        wearPatch++
        wearCode++
    }
}

val versionMajor = versionProps.getProperty("VERSION_MAJOR")?.toIntOrNull() ?: 1
val versionMinor = versionProps.getProperty("VERSION_MINOR")?.toIntOrNull() ?: 0

if (buildsApp || buildsWear) {
    versionProps.setProperty("VERSION_MAJOR", versionMajor.toString())
    versionProps.setProperty("VERSION_MINOR", versionMinor.toString())
    versionProps.setProperty("APP_VERSION_PATCH", appPatch.toString())
    versionProps.setProperty("WEAR_VERSION_PATCH", wearPatch.toString())
    versionProps.setProperty("APP_VERSION_CODE", appCode.toString())
    versionProps.setProperty("WEAR_VERSION_CODE", wearCode.toString())
    versionFile.outputStream().use { versionProps.store(it, "Updated by the build") }
}

extra["appVersionName"] = "$versionMajor.$versionMinor.$appPatch"
extra["appVersionCode"] = appCode
extra["wearVersionName"] = "$versionMajor.$versionMinor.$wearPatch"
extra["wearVersionCode"] = wearCode

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        autoCorrect = false
    }
}