plugins {
    alias(libs.plugins.kotlin.jvm) apply false
}

group = "com.impsa.otuskotlin.practice"
version = "0.0.1"


subprojects {
    repositories {
        mavenCentral()
    }
    group = rootProject.group
    version = rootProject.version

}