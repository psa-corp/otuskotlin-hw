plugins {
    id("plugin-jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.jackson.kotlin)
    implementation(libs.jackson.datatype)

    implementation(libs.coroutines.reactor)
    testImplementation(libs.coroutines.test)
    testImplementation(kotlin("test-junit"))
}