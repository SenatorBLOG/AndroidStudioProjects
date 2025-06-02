plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.breathe"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.breathe"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
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
    implementation(libs.material) // [Проверка] Убедитесь, что версия в libs.versions.toml = "1.12.0"
    implementation ("androidx.datastore:datastore-preferences:1.0.0")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.tooling)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat) // [Проверка] Может быть не нужно для чистого Compose
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.datastore.preferences) // [Исправление] Единая зависимость вместо двух
    // implementation(libs.androidx.compiler) // [Удалено] Не нужно, если не используется отдельно

    debugImplementation(libs.compose.tooling.debug)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)

}
