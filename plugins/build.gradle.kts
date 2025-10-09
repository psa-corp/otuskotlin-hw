plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("plugin-jvm") {
            id = "plugin-jvm"
            implementationClass = "com.impsa.otuskotlin.plugins.PluginJvm"
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

group = "com.impsa.otuskotlin.plugins"
version = "0.0.1"
