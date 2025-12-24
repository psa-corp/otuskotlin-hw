plugins {
    alias(libs.plugins.kotlin.jvm) apply false
}

group = "net.otuskotlin.ingredientscan.tests"
version = "0.0.1"

subprojects {
    repositories {
        mavenCentral()
    }
    group = rootProject.group
    version = rootProject.version
}

ext {
    val specDir = layout.projectDirectory.dir("../specs")
    set("spec-v1-external", specDir.file("specs-external-api-v1.yaml").toString())
    set("spec-v1-internal", specDir.file("specs-internal-api-v1.yaml").toString()) // TODO пока не трогаем
}

tasks {
    arrayOf("build", "clean", "check").forEach {tsk ->
        register(tsk) {
            group = "build"
            dependsOn(subprojects.map {  it.getTasksByName(tsk,false)})
        }
    }

    register("e2eTests") {
        dependsOn(project(":e2e-be").tasks.getByName("testAll"))
    }
}
