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

val releaseStoreFilePath: String = localProps.getProperty("RELEASE_STORE_FILE") ?: ""

android {
    namespace  = "com.example.breathe"
    compileSdk = 35

    defaultConfig {
        applicationId = "app.breatheonline.breathe"
        minSdk        = 26
        targetSdk     = 35
        versionCode   = 1
        versionName   = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Mapbox public access token baked into BuildConfig at build time.
        // Token is read from local.properties (never committed to source control).
        buildConfigField(
            "String",
            "MAPBOX_ACCESS_TOKEN",
            "\"${localProps.getProperty("MAPBOX_ACCESS_TOKEN") ?: ""}\"",
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

    composeOptions {
        kotlinCompilerExtensionVersion = "2.0.20"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
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
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation-layout:1.10.5")
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
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // ── Hilt ──────────────────────────────────────────────────────────────────
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    // ── WorkManager ───────────────────────────────────────────────────────────
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // ── Material ──────────────────────────────────────────────────────────────
    implementation("com.google.android.material:material:1.12.0")

    // ── Credential Manager for Google Sign-In ────────────────────────────────
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // ── Mapbox Maps SDK ───────────────────────────────────────────────────────
    implementation("com.mapbox.maps:android:11.8.0")

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
