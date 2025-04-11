pluginManagement {
    repositories {
        maven("https://maven.wagyourtail.xyz/releases")
        gradlePluginPortal()
        mavenCentral()
    }

    includeBuild("../")
}


plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    id("xyz.wagyourtail.manifold-settings")
}

manifold {
    subprojectPreprocessor {
        sourceSet("main")
        buildFile("debug.gradle.kts")
        ideActiveSubproject = "debug-true"

        project("debug-true") {
            property("DEBUG", true)
        }

        project("debug-false") {
            property("DEBUG", false)
        }

    }
}

rootProject.name = "test-project"