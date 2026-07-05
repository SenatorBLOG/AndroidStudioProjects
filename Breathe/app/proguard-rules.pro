# ── Stack trace readability ───────────────────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Strip Log.d / Log.v from release (privacy + log size) ────────────────────
# Log.e / Log.w are kept (they go to Crashlytics if wired).
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}

# ── Kotlin ────────────────────────────────────────────────────────────────────
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings { <fields>; }

# ── Retrofit2 ────────────────────────────────────────────────────────────────
-keepattributes Signature, InnerClasses, EnclosingMethod, Exceptions
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# ── OkHttp3 ──────────────────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# ── Gson ─────────────────────────────────────────────────────────────────────
-keepattributes *Annotation*
-dontwarn com.google.gson.**
-keep class com.google.gson.** { *; }
# Keep all data classes serialized/deserialized by Gson
-keep class com.breatheonline.breathe.data.models.** { *; }
-keepclassmembers class com.breatheonline.breathe.data.models.** { *; }

# ── Hilt / Dagger ─────────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-dontwarn dagger.hilt.**

# ── Room ──────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.**

# ── WorkManager ───────────────────────────────────────────────────────────────
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-dontwarn androidx.work.**

# ── Mapbox ────────────────────────────────────────────────────────────────────
-keep class com.mapbox.** { *; }
-dontwarn com.mapbox.**

# ── Jetpack Compose ───────────────────────────────────────────────────────────
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ── Navigation Compose ────────────────────────────────────────────────────────
-keep class androidx.navigation.** { *; }

# ── AndroidX Credentials (Google Sign-In) ─────────────────────────────────────
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.** { *; }
-dontwarn com.google.android.libraries.identity.**

# ── Coroutines ────────────────────────────────────────────────────────────────
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ── Broadcast receivers / Services (Hilt injected) ───────────────────────────
-keep class com.breatheonline.breathe.receiver.** { *; }
-keep class com.breatheonline.breathe.worker.**   { *; }

# ── Enums (used in sealed classes and API models) ────────────────────────────
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ── Parcelable ────────────────────────────────────────────────────────────────
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# ── WebView JavaScript interface (ArticleScreen uses WebView but no JS bridge) -
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
