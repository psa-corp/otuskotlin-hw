plugins {
    id("plugin-jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.jackson.kotlin)
    implementation(libs.jackson.datatype)

    implementation(projects.api.apiLog1)
    implementation(projects.api.apiV1ExternalJackson)
    implementation(projects.api.apiV1ExternalMappers)
    implementation(projects.core.coreCommon)
    implementation(projects.core.coreStubs)
    implementation(projects.biz.bizCommon)
    implementation(libs.coroutines.reactor)

    testImplementation(kotlin("test-junit"))
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
}