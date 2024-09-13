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
        maven("https://jitpack.io")
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://www.jitpack.io") }
        maven { url = uri("https://maven.aliyun.com/nexus/content/repositories/jcenter") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://www.jitpack.io") }
        maven { url = uri("https://maven.aliyun.com/nexus/content/repositories/jcenter") }
    }
}

rootProject.name = "MyChatApp"
include(":app")
include(":common")
include(":api")
include(":database")
