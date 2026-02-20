plugins {
    id("plugin-jvm")
    alias(libs.plugins.openapi.generator)
}

sourceSets {
    main {
        java.srcDir(layout.buildDirectory.dir("generate-resources/main/src/main/kotlin"))
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
}

/**
 * Настраиваем генерацию здесь
 */
openApiGenerate {

    //TODO: пусть вся генерация будет в одном месте.
    // Хорошо для совместимости, но плохо для миграции на другой framework
    // в любом случае доработка будет минимальная
    val openapiGroup = "${rootProject.group}.api.v1.internal"
    generatorName.set("kotlin-spring")
    packageName.set(openapiGroup)
    apiPackage.set("$openapiGroup.api")
    modelPackage.set("$openapiGroup.models")
    invokerPackage.set("$openapiGroup.invoker")

    inputSpec.set(rootProject.ext["spec-v1-internal"] as String)

    templateDir.set(layout.projectDirectory.dir("src/main/resources/openapi/templates").asFile.absolutePath)

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
            "library" to "spring-boot",
            "dateLibrary" to "java8",
            "enumPropertyNaming" to "UPPERCASE",
            "serializationLibrary" to "jackson",
            "collectionType" to "list",

            "useSpringBoot3" to "true",
            "interfaceOnly" to "true",
            "reactive" to "true",
            "useCoroutines" to "true",
            "useTags" to "true",
            "delegatePattern" to "false",
            "skipDefaultInterface" to "true",
        )
    )

    typeMappings.set(
        mapOf(
            "binary" to "org.springframework.core.io.Resource", // для скачивания файлов
            "string" to "kotlin.String"
        )
    )

    importMappings.set(
        mapOf(
            "org.springframework.web.multipart.MultipartFile" to "org.springframework.web.multipart.MultipartFile",
            "java.time.OffsetDateTime" to "java.time.OffsetDateTime",
            "java.time.LocalDate" to "java.time.LocalDate"
        )
    )
    library.set("spring-boot")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.coroutines.reactor)

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
