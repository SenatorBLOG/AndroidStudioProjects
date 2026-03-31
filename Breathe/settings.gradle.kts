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

// Read secrets from local.properties (gitignored)
val localProps = Properties().also { props ->
    rootDir.resolve("local.properties").takeIf { it.exists() }
        ?.inputStream()?.use { props.load(it) }
}

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
                password = localProps.getProperty("MAPBOX_DOWNLOADS_TOKEN") ?: ""
            }
        }
    }
}

rootProject.name = "Breathe"
include(":app")
