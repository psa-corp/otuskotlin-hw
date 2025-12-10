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
 * Настраиваем генерацию здесь
 */
openApiGenerate {

    //TODO: пусть вся генерация будет в одном месте.
    // Хорошо для совместимости, но плохо для миграции на другой framework
    val openapiGroup = "${rootProject.group}.api.v1.external"
    generatorName.set("kotlin-spring") // Это и есть активный генератор
    packageName.set(openapiGroup)
    apiPackage.set("$openapiGroup.api")
    modelPackage.set("$openapiGroup.models")
    invokerPackage.set("$openapiGroup.invoker")

    inputSpec.set(rootProject.ext["spec-v1-external"] as String) // <-

    /**
     * Здесь указываем, что нам нужны только модели, все остальное не нужно
     * https://openapi-generator.tech/docs/globals
     */
    globalProperties.apply {
        put("models", "")
        put("apis", "")
        put("modelDocs", "false")
//        put("apiDocs", "false")
    }

    /**
     * Настройка дополнительных параметров из документации по генератору
     * https://github.com/OpenAPITools/openapi-generator/blob/master/docs/generators/kotlin.md
     */
    configOptions.set(
        mapOf(
            "dateLibrary" to "java8",
            "enumPropertyNaming" to "UPPERCASE",
            "serializationLibrary" to "jackson",
            "collectionType" to "list",

            "useSpringBoot3" to "true", // добавляем для Spring Boot 3
            "interfaceOnly" to "true",   // генерируем интерфейсы API
            "reactive" to "false",
            "serviceInterface" to "false",
            "useTags" to "true",
            "delegatePattern" to "false",
            "skipDefaultInterface" to "true"
        )
    )

    // Для multipart запросов
    typeMappings.set(
        mapOf(
            "file" to "org.springframework.web.multipart.MultipartFile"
        )
    )

    importMappings.set(
        mapOf(
            "org.springframework.web.multipart.MultipartFile" to "org.springframework.web.multipart.MultipartFile",
            "java.time.OffsetDateTime" to "java.time.OffsetDateTime",
            "java.time.LocalDate" to "java.time.LocalDate"
        )
    )
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.jackson.kotlin)
    implementation(libs.jackson.datatype)
    implementation(libs.swagger.core)
    testImplementation(kotlin("test-junit"))
}

tasks {
    compileKotlin {
        dependsOn(openApiGenerate)
    }
}
