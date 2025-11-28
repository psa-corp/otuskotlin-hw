enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

pluginManagement {
    includeBuild("../plugins")
    plugins {
        id("plugin-jvm") apply false

    }
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}


plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "ingredient-scan"

// --- Остальные модули ---
include(":scanner") // Тоже лучше использовать kebab-case
//include(":app:app-common")
include(":core-common")
include(":api:api-v1-external-jackson")

