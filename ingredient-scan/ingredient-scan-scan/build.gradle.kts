plugins {
	id("plugin-jvm")
	id("idea")
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.spring.boot)
	alias(libs.plugins.spring.dependency.management)
	alias(libs.plugins.jib)
}

group = "net.otuskotlin.ingredient-scan"
version = "0.0.1"
description = "Spring ingredient-scan-scan "

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
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
		    username = "darthchain"
		    password = "{Djorjik12}"
		 }

	}
	container {
		ports = listOf("8080")
		creationTime = "USE_CURRENT_TIMESTAMP"
	}
}
