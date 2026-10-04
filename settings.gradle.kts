pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TataAndroid"

include(":app")
include(":shared")

include(":identity")
include(":carelink")
include(":treatment")
include(":intake")
include(":omission")
include(":monitoring")
include(":analytics")
include(":inventory")
include(":preferences")
