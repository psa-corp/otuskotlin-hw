plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.jib) apply false
}

group = "net.otuskotlin.ingredientscan"
version = "0.0.1"

subprojects {
    repositories {
        mavenCentral()
    }
    group = rootProject.group
    version = rootProject.version
}

ext {
    val specDir = layout.projectDirectory.dir("../specs")
    set("spec-v1-external", specDir.file("specs-external-api-v1.yaml").toString())
    set("spec-v1-internal", specDir.file("specs-internal-api-v1.yaml").toString())
    set("spec-log1", specDir.file("specs-scan-log1.yaml").toString())
}
