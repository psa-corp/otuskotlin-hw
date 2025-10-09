package net.otuskotlin.lessons.m2l1.demo

fun String.modify(modification: String.() -> String): String {
    return this.modification()
}

fun main() {
    val result = "Hello".modify { uppercase() }

    println(result)
}
