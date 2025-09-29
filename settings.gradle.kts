pluginManagement{
    val kotlinVersion: String by settings
    plugins{
        kotlin("jvm") version kotlinVersion
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "otuskotlin"

include("m1l1-first")
include("m1l2-basic")
include("m1l3-func")
include("m1l4-oop")