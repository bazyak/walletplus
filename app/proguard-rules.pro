# ProGuard rules for Wallet App
# Keep line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep annotations
-keepattributes *Annotation*

# Keep generic signatures for reflection
-keepattributes Signature

# Keep exceptions
-keepattributes Exceptions

# =========================
# Data Models
# =========================
# Keep all data models - they're serialized/deserialized
-keep class com.bazyak.walletplus.data.model.** { *; }

# Keep PKPass parser models for Gson deserialization
-keep class com.bazyak.walletplus.data.parser.pkpass.** { *; }

# =========================
# Kotlin
# =========================
# Keep Kotlin metadata for reflection
-keep class kotlin.Metadata { *; }

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keepclassmembers class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# Keep continuation for coroutines
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# =========================
# Gson
# =========================
# Keep generic type information for Gson
-keepattributes Signature

# Keep Gson classes
-keep class com.google.gson.** { *; }

# Keep fields for Gson serialization - DO NOT allow obfuscation
-keepclassmembers class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Prevent obfuscation of classes with Gson annotations
-keep class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Keep all fields and constructors in data model classes for Gson
-keepclassmembers class com.bazyak.walletplus.data.model.** {
  <fields>;
  <init>(...);
}

# Prevent R8 from optimizing away default constructors needed by Gson
-keepclassmembers class com.bazyak.walletplus.data.model.** {
  <init>();
}

# =========================
# Retrofit & OkHttp
# =========================
# Retrofit
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit

-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# =========================
# Room Database
# =========================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

-keepclassmembers class * extends androidx.room.RoomDatabase {
    public static ** Companion;
}

# Keep Room DAO interfaces
-keep interface * extends androidx.room.Dao

# =========================
# Jetpack Compose
# =========================
# Keep Composable functions
-keep @androidx.compose.runtime.Composable class ** { *; }

# Keep Compose runtime classes
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.material3.** { *; }

# Keep remember functions
-keepclassmembers class androidx.compose.** {
    ** remember*(...);
}

# =========================
# ZXing (Barcode Library)
# =========================
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# =========================
# Coil (Image Loading)
# =========================
-keep class coil.** { *; }
-keep interface coil.** { *; }
-dontwarn coil.**

# =========================
# AndroidX
# =========================
# Keep AndroidX classes
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

# Keep lifecycle classes
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
}

# =========================
# WorkManager
# =========================
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# =========================
# Reorderable Library
# =========================
-keep class org.burnoutcrew.reorderable.** { *; }

# =========================
# QRose (QR Code Generation)
# =========================
-keep class io.github.alexzhirkevich.qrose.** { *; }
-dontwarn io.github.alexzhirkevich.qrose.**

# =========================
# General Android
# =========================
# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep setters for animations
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

# Keep Parcelable implementations
-keepclassmembers class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator CREATOR;
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# =========================
# Optimization Settings
# =========================
# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Optimize
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
