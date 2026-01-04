package net.otuskotlin.ingredientscan.scanner.controllers

import net.otuskotlin.ingredientscan.api.v1.external.api.DownloadApi
import net.otuskotlin.ingredientscan.scanner.services.biz.BizService
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController


@RestController
open class DownloadController(private val bizService: BizService) : DownloadApi  {

    override suspend fun downloadFile(fileName: String): ResponseEntity<Resource> {
        return bizService.get(fileName)
    }
}