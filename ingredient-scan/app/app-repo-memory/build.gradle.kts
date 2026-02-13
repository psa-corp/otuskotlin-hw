import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
plugins {
    id("plugin-jvm")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.dependency.management)
}

group = "net.otuskotlin.ingredientscan.app.repomemory"
version = rootProject.version

dependencies {
    testImplementation(platform(libs.junit.bom))

    implementation(platform("org.springframework.boot:spring-boot-dependencies:${libs.versions.spring.boot.get()}"))

    implementation(kotlin("stdlib"))
    implementation(projects.core.coreCommon)
    implementation("org.springframework:spring-context")
    implementation(libs.slf4j.api)
    implementation(libs.coroutines.core)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.assertj.core)

    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.junit.platform") {
            useVersion(libs.versions.junit.platform.get())
        }
    }
}
