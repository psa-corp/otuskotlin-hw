plugins {
    alias(libs.plugins.kotlin.jvm) apply false
}

group = "net.otuskotlin.ingredient_scan_libs"
version = "0.0.1"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version

}

ext {
    val specDir = layout.projectDirectory.dir("../specs")
    set("spec-v1-external", specDir.file("specs-external-api-v1.yaml").toString())
    set("spec-v1-internal", specDir.file("specs-internal-api-v1.yaml").toString())
}

tasks {
    arrayOf("build", "clean", "check").forEach {tsk ->
        register(tsk ) {
            group = "build"
            dependsOn(subprojects.map {  it.getTasksByName(tsk,false)})
        }
    }
}

