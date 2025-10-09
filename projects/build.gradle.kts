plugins {
    alias(libs.plugins.kotlin.jvm) apply false
}

group = "net.otuskotlin.projects"
version = "0.0.1"


subprojects {
    repositories {
        mavenCentral()
    }
    group = rootProject.group
    version = rootProject.version

}