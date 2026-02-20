plugins {
    id("plugin-jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.jackson.kotlin)
    implementation(libs.jackson.datatype)
    implementation(projects.core.coreCommon)
    implementation(projects.core.coreStubs)
    implementation(projects.core.coreCor)

}
