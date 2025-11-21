import com.google.cloud.tools.jib.gradle.JibExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
//    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.jib) apply false
}

group = "net.otuskotlin.ingredient-scan"
version = "0.0.1"

subprojects {
    repositories {
        mavenCentral()
    }
    group = rootProject.group
    version = rootProject.version
}
