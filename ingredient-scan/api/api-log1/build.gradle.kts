import org.gradle.kotlin.dsl.dependencies

plugins {
    id("plugin-jvm")
    alias(libs.plugins.openapi.generator)
}

sourceSets {
    main {
        java.srcDir(layout.buildDirectory.dir("generate-resources/main/src/main/kotlin"))
    }
}

/**
 * Настраиваем генерацию моделей логов из specs-scan-log1.yaml
 */
openApiGenerate {
    val openapiGroup = "${rootProject.group}.api.log1"

    generatorName.set("kotlin")
    packageName.set(openapiGroup)
    apiPackage.set("$openapiGroup.api")
    modelPackage.set("$openapiGroup.models")
    invokerPackage.set("$openapiGroup.invoker")

    inputSpec.set(rootProject.ext["spec-log1"] as String)

    /**
     * Здесь указываем, что нам нужны только модели DTO
     */
    globalProperties.apply {
        put("models", "")
        put("modelDocs", "false")
    }

    /**
     * Настройка дополнительных параметров: Jackson и java.time
     */
    configOptions.set(
        mapOf(
            "dateLibrary" to "java8",
            "enumPropertyNaming" to "UPPERCASE",
            "serializationLibrary" to "jackson",
            "collectionType" to "list"
        )
    )
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":core-common"))

//    implementation(project(":api-log1"))
    implementation(libs.jackson.kotlin)
    implementation(libs.jackson.datatype)

    testImplementation(kotlin("test-junit"))
}

tasks {
    compileKotlin {
        dependsOn(openApiGenerate)
    }
}