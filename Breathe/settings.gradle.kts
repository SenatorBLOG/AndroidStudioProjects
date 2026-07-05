import java.util.Properties

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// Read secrets from local.properties (gitignored)
val localProps = Properties().also { props ->
    rootDir.resolve("local.properties").takeIf { it.exists() }
        ?.inputStream()?.use { props.load(it) }
}

val mapboxDownloadsToken =
    System.getenv("MAPBOX_DOWNLOADS_TOKEN")
        ?: System.getenv("MAPBOX_SECRET_TOKEN")
        ?: localProps.getProperty("MAPBOX_DOWNLOADS_TOKEN")
        ?: localProps.getProperty("MAPBOX_SECRET_TOKEN")
        ?: ""

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Mapbox Maps SDK — token stored in local.properties (gitignored)
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication { create<BasicAuthentication>("basic") }
            credentials {
                username = "mapbox"
                password = mapboxDownloadsToken
            }
        }
    }
}

rootProject.name = "Breathe"
include(":app")
