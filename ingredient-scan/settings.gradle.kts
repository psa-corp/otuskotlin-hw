enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

pluginManagement {
    includeBuild("../plugins")
    plugins {
        id("plugin-jvm") apply false

    }
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}


plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "ingredient-scan"

// --- Подключение библиотеки ---

// 1. Включаем модуль.
// Я рекомендую называть его так, как он называется в библиотеке (:logging-common),
// либо дать префикс (:libs:logging-common), если хотите структуру.
include(":libs:logging-common")

// 2. Указываем физический путь к папке, которую мы переименовали в шаге 2А.
project(":libs:logging-common").projectDir = file("../ingredient-scan-libs/logging-common")

// --- Остальные модули ---
include(":scanner") // Тоже лучше использовать kebab-case
include(":app:app-common")
include(":core-common")
