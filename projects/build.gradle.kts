plugins {
    alias(libs.plugins.kotlin.jvm) apply false
}

group = "com.impsa.otuskotlin.projects"
version = "0.0.1"


subprojects {
    repositories {
        mavenCentral()
    }
    group = rootProject.group
    version = rootProject.version

}