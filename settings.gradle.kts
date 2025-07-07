pluginManagement {
    repositories {
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev")}
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://www.jitpack.io")}
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev")}
        google()
        mavenCentral()
    }
}

rootProject.name = "WavePark"
include(":app")
include(":shared")
include(":web")
include(":iosApp")
 