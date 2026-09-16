# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Production ProGuard / R8 Rules for Ren

# Line numbers & debugging attributes
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep app data & domain models (Room, Moshi, Firestore, serialization)
-keep class com.example.data.model.** { *; }
-keep class com.example.domain.model.** { *; }
-keep class com.example.data.local.** { *; }

# Room
-keep class androidx.room.RoomDatabase { *; }
-dontwarn androidx.room.paging.**

# Koin Dependency Injection
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}

# Coil
-keep class coil.** { *; }
-dontwarn coil.**
