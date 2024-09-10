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
        //maven ("https://www.jitpack.io")
        maven ("https://jitpack.io")
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://www.jitpack.io") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        //maven ("https://www.jitpack.io")
        maven ("https://jitpack.io")
        maven { url = uri("https://www.jitpack.io") }
    }
}

rootProject.name = "MyChatApp"
include(":app")
include(":common")
include(":api")
include(":database")
