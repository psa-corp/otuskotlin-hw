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
	implementation(projects.api.apiV1InternalJackson)
	implementation(projects.api.apiV1InternalMappers)
	implementation(projects.core.coreCommon)
	implementation(projects.core.coreStubs)
	implementation(projects.biz.bizCommon)
	implementation(projects.biz.bizCommon)
	implementation(projects.app.appCommon)
	implementation(projects.app.appContent)
	implementation(projects.app.appInternal)
	implementation(projects.app.appRepoMemory)
//	implementation(projects.app.appRepoPostgres)

	implementation(libs.spring.boot.starter.webflux)
	implementation(libs.spring.boot.starter.actuator)
	implementation(libs.spring.boot.starter.validation)
	implementation(libs.spring.boot.docker.compose)
	implementation(libs.coroutines.reactor)
	implementation(libs.coroutines.core)

	implementation(libs.spring.kafka)
	implementation(libs.kafka.streams)
	implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")

	implementation("org.postgresql:r2dbc-postgresql")
	runtimeOnly("org.postgresql:postgresql")

	implementation("io.r2dbc:r2dbc-pool")

	implementation("org.liquibase:liquibase-core")
	implementation("org.springframework.boot:spring-boot-starter-jdbc")

	implementation(libs.jackson.kotlin)
	implementation(libs.jackson.datatype)

	implementation(libs.coroutines.core)
	implementation(libs.coroutines.reactor)
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")


	implementation(libs.springdoc.openapi)
	implementation(libs.spring.cloud.aws)

	implementation(libs.jackson.kotlin)
	implementation(libs.jackson.datatype)

	implementation(libs.software.amazon)
	implementation(libs.software.amazon.client)
	implementation(libs.software.amazon.transfer.manager)
	implementation(libs.software.amazon.aws.crt)


	implementation(libs.swagger.core)

	implementation(libs.github.caffeine)

	testImplementation(libs.spring.boot.starter.test)
	testImplementation(libs.spring.boot.starter.webflux)
	testImplementation(libs.coroutines.test)
	testImplementation(libs.coroutines.reactor)
	testImplementation(libs.bundles.testcontainers)
	testImplementation(libs.testcontainers.minio)
	testImplementation(libs.okhttp)

	testImplementation(libs.jackson.databind)
	testImplementation(libs.jackson.kotlin)
	testImplementation(libs.jackson.datatype)

	testImplementation(libs.assertj.core)
	testImplementation(libs.assertk)
	testImplementation(libs.mockito.kotlin)

	testImplementation(libs.spring.kafka.test)
	testImplementation(libs.kafka.streams.test)
	testImplementation(libs.projectreactor.reactor.test)
	testImplementation(libs.mockk.mockk)


}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll(
			"-Xjsr305=strict",
			"-Xannotation-default-target=param-property"
		)
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
//	jvmArgs = listOf("-XX:+EnableDynamicAgentLoading")
}

jib {
	from {
		image = "eclipse-temurin:${libs.versions.jreImage.get()}"
		platforms {
			platform {
				architecture = "amd64"
				os = "linux"
			}
		}
	}

	to {
		image = "darthchain/ingredient-scan-scan-hw"
		tags = setOf("latest", version.toString())

		 auth {
		    username = ""
		    password = ""
		 }
	}

	container {
		ports = listOf("8080")
		creationTime = "USE_CURRENT_TIMESTAMP"

		environment = mapOf(
			"SPRING_PROFILES_ACTIVE" to "prod",
			"SPRING_BOOT_DOCKER_COMPOSE_ENABLED" to "false",
			"JAVA_TOOL_OPTIONS" to "-XX:+UseG1GC -XX:MaxRAMPercentage=75.0"
		)

		labels = mapOf(
			"maintainer" to "psa",
			"version" to version.toString(),
			"environment" to "production"
		)

		user = "nobody"
	}

	containerizingMode = "packaged"
}