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

rootProject.name = "ingredient-scan-tests"


includeBuild("../ingredient-scan") {
    dependencySubstitution {
        substitute(module("net.otuskotlin.ingredientscan:core-common")).using(project(":core-common"))
        substitute(module("net.otuskotlin.ingredientscan:core-stubs")).using(project(":core-stubs"))
        substitute(module("net.otuskotlin.ingredientscan:api-v1-external-jackson")).using(project(":api-v1-external-jackson"))
        substitute(module("net.otuskotlin.ingredientscan:api-v1-external-mappers")).using(project(":api-v1-external-mappers"))
    }
}

include(":e2e-be")
