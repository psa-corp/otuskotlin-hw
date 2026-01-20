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
    implementation(projects.app.appCommon)

    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.coroutines.reactor)
    testImplementation(libs.coroutines.test)
    testImplementation(kotlin("test-junit"))
}