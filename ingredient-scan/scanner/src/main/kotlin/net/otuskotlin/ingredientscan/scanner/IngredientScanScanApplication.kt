package net.otuskotlin.ingredientscan.scaner

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class IngredientScanScanApplication

fun main(args: Array<String>) {
	runApplication<IngredientScanScanApplication>(*args)
}
