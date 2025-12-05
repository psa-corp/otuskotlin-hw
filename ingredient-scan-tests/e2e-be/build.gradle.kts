plugins {
    id("plugin-jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    testImplementation(platform(libs.junit.bom))

    // Добавляем нужные модули без указания версий
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)

    testImplementation(libs.bundles.testcontainers)
    testImplementation(libs.okhttp)
    testImplementation(libs.jackson.kotlin)
    testImplementation(libs.slf4j.simple)


    testImplementation(libs.assertj.core)
    testImplementation(libs.assertk)
}

tasks {
    withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = true
        }
        systemProperty("junit.jupiter.testinstance.lifecycle.default", "per_class")
    }

    register<Test>("testWiremock") {
        group = "verification"
        description = "Runs WireMock integration tests"
        filter {
            includeTestsMatching("*Wiremock*")
        }
    }
}