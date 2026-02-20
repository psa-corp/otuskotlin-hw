plugins {
    id("plugin-jvm")
}

group = rootProject.group
version = rootProject.version

dependencies {
    implementation(kotlin("stdlib"))
    implementation(projects.api.apiV1InternalJackson)
    implementation(projects.core.coreCommon)
    implementation(projects.core.coreStubs)
    testImplementation(kotlin("test-junit"))
}