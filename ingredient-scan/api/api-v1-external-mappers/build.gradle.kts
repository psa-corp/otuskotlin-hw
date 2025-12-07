plugins {
    id("plugin-jvm")
}

group = rootProject.group
version = rootProject.version

dependencies {
    implementation(kotlin("stdlib"))
//    implementation(libs.kotlinx.datetime)
    implementation(projects.api.apiV1ExternalJackson)
    implementation(projects.coreCommon)
    testImplementation(kotlin("test-junit"))
}