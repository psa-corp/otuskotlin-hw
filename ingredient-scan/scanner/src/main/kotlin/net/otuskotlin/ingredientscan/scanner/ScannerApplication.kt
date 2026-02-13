package net.otuskotlin.ingredientscan.scanner

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
	scanBasePackages = [
		"net.otuskotlin.ingredientscan.scanner",
		"net.otuskotlin.ingredientscan.app.repo.memory"
	]
)
open class ScannerApplication

fun main(args: Array<String>) {
	runApplication<ScannerApplication>(*args)
}
