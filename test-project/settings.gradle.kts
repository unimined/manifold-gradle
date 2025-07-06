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
    id("xyz.wagyourtail.manifold")
}

manifold {
    subprojectPreprocessor {
        sourceSet("main")
        buildFile("debug.gradle.kts")
        ideActiveSubproject = "debug-true"

        project("debug-true") {
            property("DEBUG", 2)
        }

        project("debug-false") {
            property("DEBUG", 1)
        }

    }
}

rootProject.name = "test-project"