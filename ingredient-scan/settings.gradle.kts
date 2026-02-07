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

include(":core:core-common")
include(":core:core-stubs")
include(":core:core-cor")
include(":api:api-v1-external-jackson")
include(":api:api-v1-external-mappers")
include(":api:api-v1-internal-jackson")
include(":api:api-v1-internal-mappers")
include(":api:api-log1")
include(":app:app-common")
include(":app:app-content")
include(":scanner")
include(":analyzer")
include(":biz:biz-common")


