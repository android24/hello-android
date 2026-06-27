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

rootProject.name = "HelloTestingQualityDelivery"
include(":app")
include(":core-model")
include(":core-network")
include(":domain-course")
include(":data-course")
include(":feature-course")
