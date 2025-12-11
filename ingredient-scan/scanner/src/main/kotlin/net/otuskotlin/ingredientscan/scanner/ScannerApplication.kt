package net.otuskotlin.ingredientscan.scanner

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class ScannerApplication

fun main(args: Array<String>) {
	runApplication<ScannerApplication>(*args)
}
