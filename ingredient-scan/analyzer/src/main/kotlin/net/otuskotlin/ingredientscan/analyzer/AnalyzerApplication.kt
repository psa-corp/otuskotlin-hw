package net.otuskotlin.ingredientscan.analyzer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class AnalyzerApplication

fun main(args: Array<String>) {
	runApplication<AnalyzerApplication>(*args)
}
