# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep Compose runtime classes
-keep class androidx.compose.** { *; }
-keep interface androidx.compose.** { *; }

# Keep design system public API
-keep public class com.bazyak.walletplus.designsystem.** { *; }
