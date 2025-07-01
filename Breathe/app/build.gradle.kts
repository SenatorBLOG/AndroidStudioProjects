plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    id("com.google.dagger.hilt.android")
    kotlin("kapt")
}

android {
    namespace = "com.example.breathe"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.breathe"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        // Match Compose Compiler to Kotlin 2.0.21
        kotlinCompilerExtensionVersion = "2.0.1"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Jetpack Compose BOM (2025.05.01)
    implementation(platform("androidx.compose:compose-bom:2025.05.01"))

    // Core AndroidX
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-compose:1.10.1")

    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.9.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    // Room
    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-common:2.7.2")
    kapt("androidx.room:room-compiler:2.7.2")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")

    // Material (Optional if using M3 already)
    implementation("com.google.android.material:material:1.12.0")

    // Data Transport
    implementation("com.google.android.datatransport:transport-runtime:4.0.0")

    // Tests
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
