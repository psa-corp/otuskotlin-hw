plugins {
	id("plugin-jvm")
	id("idea")
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.spring.boot)
	alias(libs.plugins.spring.dependency.management)
	alias(libs.plugins.jib)
}

group = "net.otuskotlin.ingredientscan.scanner"
version = "0.0.1"
description = "Spring ingredient-scan scanner"

dependencies {
	implementation(kotlin("stdlib"))
	implementation(projects.api.apiLog1)
	implementation(projects.api.apiV1ExternalJackson)
	implementation(projects.api.apiV1ExternalMappers)
	implementation(projects.coreCommon)

	implementation(libs.spring.boot.starter.web)
	implementation(libs.spring.boot.starter.actuator)
	implementation(libs.spring.boot.starter.validation)
	implementation(libs.spring.boot.docker.compose)

	implementation(libs.spring.kafka)
	implementation(libs.kafka.streams)

	implementation(libs.springdoc.openapi)
	implementation(libs.spring.cloud.aws)

	implementation(libs.jackson.kotlin)
	implementation(libs.jackson.datatype)

	implementation(libs.software.amazon)
	implementation(libs.swagger.core)

	implementation(libs.github.caffeine)

	testImplementation(libs.spring.boot.starter.test)

	testImplementation(libs.bundles.testcontainers)
	testImplementation(libs.testcontainers.minio)
	testImplementation(libs.okhttp)

	testImplementation(libs.jackson.databind)
	testImplementation(libs.jackson.kotlin)
	testImplementation(libs.jackson.datatype)

	testImplementation(libs.assertj.core)
	testImplementation(libs.assertk)
	testImplementation(libs.mockito.kotlin)

}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
	jvmArgs = listOf("-XX:+EnableDynamicAgentLoading")
}

jib {
	from {
		image = "eclipse-temurin:${libs.versions.jreImage.get()}"
	}
	to {
		image = "darthchain/ingredient-scan-scan"
		tags = setOf("latest", version.toString())
		auth {
			username = ""
			password = ""
		}

	}
	container {
		ports = listOf("8080")
		creationTime = "USE_CURRENT_TIMESTAMP"
	}
}