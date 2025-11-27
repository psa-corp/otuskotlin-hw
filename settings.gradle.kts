
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "otuskotlin"

includeBuild("lessons")
includeBuild("plugins")
includeBuild("ingredient_scan")
includeBuild("ingredient_scan_libs")