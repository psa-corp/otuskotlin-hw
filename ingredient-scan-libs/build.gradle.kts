plugins {
    alias(libs.plugins.kotlin.jvm) apply false
}

// Это база для всех артефактов этой сборки
group = "net.otuskotlin.ingredientscan.libs" // Можно использовать дефис или слитное написание
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
    set("spec-log1", specDir.file("specs-scan-log1.yaml").toString())
}

tasks {
    arrayOf("build", "clean", "check").forEach {tsk ->
        register(tsk ) {
            group = "build"
            dependsOn(subprojects.map {  it.getTasksByName(tsk,false)})
        }
    }
}

