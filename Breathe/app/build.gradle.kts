import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

// ── Local properties (gitignored) ─────────────────────────────────────────────
val localProps = Properties().also { props ->
    rootProject.file("local.properties").takeIf { it.exists() }
        ?.inputStream()?.use { props.load(it) }
}
val mapboxAccessToken =
    System.getenv("MAPBOX_ACCESS_TOKEN")
        ?: localProps.getProperty("MAPBOX_ACCESS_TOKEN")
        ?: ""

val releaseStoreFilePath: String = localProps.getProperty("RELEASE_STORE_FILE") ?: ""

val googleWebClientId =
    System.getenv("GOOGLE_WEB_CLIENT_ID")
        ?: localProps.getProperty("GOOGLE_WEB_CLIENT_ID")
        ?: "617412317511-19s97rms2r9t3ihl041h7k128a7pqd98.apps.googleusercontent.com"

android {
    namespace  = "com.breatheonline.breathe"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.breatheonline.breathe"
        minSdk        = 26
        targetSdk     = 35
        versionCode   = 2
        versionName   = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Mapbox public access token baked into BuildConfig at build time.
        // Token is read from local.properties (never committed to source control).
        buildConfigField(
            "String",
            "MAPBOX_ACCESS_TOKEN",
            "\"$mapboxAccessToken\"",
        )
        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_ID",
            "\"$googleWebClientId\"",
        )
    }

    // ── Signing ───────────────────────────────────────────────────────────────
    // To enable release signing add these keys to local.properties:
    //   RELEASE_STORE_FILE=path/to/your.keystore
    //   RELEASE_STORE_PASSWORD=yourStorePassword
    //   RELEASE_KEY_ALIAS=yourKeyAlias
    //   RELEASE_KEY_PASSWORD=yourKeyPassword
    if (releaseStoreFilePath.isNotEmpty()) {
        signingConfigs {
            create("release") {
                storeFile     = file(releaseStoreFilePath)
                storePassword = localProps.getProperty("RELEASE_STORE_PASSWORD") ?: ""
                keyAlias      = localProps.getProperty("RELEASE_KEY_ALIAS")      ?: ""
                keyPassword   = localProps.getProperty("RELEASE_KEY_PASSWORD")   ?: ""
            }
        }
    }

    // ── Build types ───────────────────────────────────────────────────────────
    buildTypes {
        release {
            isMinifyEnabled   = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (releaseStoreFilePath.isNotEmpty()) {
                signingConfig = signingConfigs.getByName("release")
            }
            // Explicit: release builds are NOT debuggable
            isDebuggable = false
        }
        debug {
            isDebuggable = true
            // Keep same applicationId so Room DB / DataStore survive reinstall during dev
        }
    }

    buildFeatures {
        compose     = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    // Exclude duplicate META-INF license files that can appear when mixing okhttp versions
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}

dependencies {
    // ── Compose BOM ───────────────────────────────────────────────────────────
    implementation(platform("androidx.compose:compose-bom:2025.05.01"))

    // ── Core AndroidX ─────────────────────────────────────────────────────────
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.activity:activity-compose:1.10.1")

    // ── Compose UI + Material3 ────────────────────────────────────────────────
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("com.composables:icons-lucide-android:1.1.0")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation-layout")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // ── Navigation Compose ────────────────────────────────────────────────────
    implementation("androidx.navigation:navigation-compose:2.9.1")

    // ── ViewModel + Lifecycle ─────────────────────────────────────────────────
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    // ── Coroutines ────────────────────────────────────────────────────────────
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // ── Retrofit2 + Gson converter ────────────────────────────────────────────
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // ── OkHttp3 (single BOM, removes the duplicate declaration) ──────────────
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

    // ── EncryptedSharedPreferences ────────────────────────────────────────────
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // ── Coil image loading ────────────────────────────────────────────────────
    implementation("io.coil-kt:coil-compose:2.7.0")

    // ── DataStore ─────────────────────────────────────────────────────────────
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    // ── Room ──────────────────────────────────────────────────────────────────
    implementation("androidx.room:room-runtime:2.7.0")
    implementation("androidx.room:room-ktx:2.7.0")
    ksp("androidx.room:room-compiler:2.7.0")

    // ── Hilt ──────────────────────────────────────────────────────────────────
    implementation("com.google.dagger:hilt-android:2.59.2")
    ksp("com.google.dagger:hilt-compiler:2.59.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")
    implementation("androidx.hilt:hilt-work:1.3.0")
    ksp("androidx.hilt:hilt-compiler:1.3.0")

    // ── WorkManager ───────────────────────────────────────────────────────────
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // ── Material ──────────────────────────────────────────────────────────────
    implementation("com.google.android.material:material:1.12.0")

    // ── Credential Manager for Google Sign-In ────────────────────────────────
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // ── Chrome Custom Tabs (OAuth browser flow) ───────────────────────────────
    implementation("androidx.browser:browser:1.8.0")

    // ── Health Connect (Amazfit · Xiaomi · Samsung health data) ──────────────
    implementation("androidx.health.connect:connect-client:1.1.0")

    // ── Mapbox Maps SDK ───────────────────────────────────────────────────────

    // ── Unit tests ────────────────────────────────────────────────────────────
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")

    // ── Instrumented tests ────────────────────────────────────────────────────
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

ksp {
    arg("room.incremental", "true")
}
