plugins {
    id("plugin-jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.kotlinx.datetime)

    // Ссылаемся по тому имени, которое задали в include в settings.gradle.kts
    implementation(project(":libs:logging-common"))
}
