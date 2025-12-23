

plugins {
    id("plugin-jvm")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

group = "net.otuskotlin.ingredientscan.tests"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {

    testImplementation(kotlin("stdlib"))

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.web)
    testImplementation(libs.spring.boot.docker.compose)

    testImplementation("net.otuskotlin.ingredientscan:core-common")
    testImplementation("net.otuskotlin.ingredientscan:api-v1-external-jackson")
    testImplementation("net.otuskotlin.ingredientscan:api-v1-external-mappers")

    testImplementation(libs.okhttp)

    testImplementation(libs.jackson.databind)
    testImplementation(libs.jackson.kotlin)
    testImplementation(libs.jackson.datatype)

    testImplementation(libs.logback.classic)

    testImplementation(libs.assertj.core)
    testImplementation(libs.assertk)


    testImplementation(libs.testcontainers.core)
    testImplementation(libs.testcontainers.junit)

    testImplementation(libs.spring.boot.testcontainers)

    testImplementation(libs.logback.access.tomcat)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks {
    bootJar {
        enabled = false
    }

    withType<Test>().configureEach {

        systemProperty("spring.docker.compose.file",
            "${projectDir.absolutePath}/docker-compose/docker-compose-test.yml")

        workingDir = projectDir

        useJUnitPlatform()

        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }

        val byteBuddyAgent = configurations.testRuntimeClasspath.get()
            .files
            .find { it.name.startsWith("byte-buddy-agent-") }

        if (byteBuddyAgent != null) {
            val agentPath = byteBuddyAgent.absolutePath
                .replace("\\", "/") // Важно для Windows
            jvmArgs = listOf("-javaagent:$agentPath")
        } else {
            logger.warn("ByteBuddy agent JAR not found in test runtime classpath.")
        }

        jvmArgs!!.addAll(listOf(
            "-XX:+EnableDynamicAgentLoading",
            "-Djdk.instrument.traceUsage=false"
        ))
    }

    register<Test>("testRest") {
        group = "verification"
        description = "Runs REST integration tests with Docker Compose"
        filter {
            includeTestsMatching("*Rest*")
        }
    }
}
