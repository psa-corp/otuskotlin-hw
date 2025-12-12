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

	implementation(projects.api.apiLog1)
	implementation(projects.api.apiV1ExternalJackson)
	implementation(projects.api.apiV1ExternalMappers)
	implementation(projects.coreCommon)

	implementation(libs.spring.boot.starter.web)
	implementation(libs.spring.boot.starter.actuator)
	implementation(libs.spring.boot.starter.validation)
	implementation(libs.jackson.kotlin)
	implementation(libs.jackson.datatype)
	implementation(libs.spring.cloud.aws)
	implementation(libs.software.amazon)
	implementation(libs.swagger.core)
	implementation(libs.springdoc.openapi)



	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
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
