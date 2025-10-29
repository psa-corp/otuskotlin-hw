plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("plugin-jvm") {
            id = "plugin-jvm"
            implementationClass = "net.otuskotlin.plugins.PluginJvm"
        }
        register("plugin-kmp") {
            id = "plugin-kmp"
            implementationClass = "net.otuskotlin.plugins.PluginMultiplatform"
        }
    }
}

repositories {
    mavenCentral()

}

dependencies {
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(libs.lkotlin)
    implementation(libs.lvalidator)
}

group = "net.otuskotlin.plugins"
version = "0.0.1"
