package net.otuskotlin.ingredientscan.scanner.controllers

import net.otuskotlin.ingredientscan.scanner.services.biz.BizService
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux


@RequestMapping("/v1")
@RestController
class DownloadController(private val bizService: BizService) {

    @GetMapping("/download/files", produces = ["application/zip"])
    suspend fun downloadAll(@RequestParam fileName: List<String>): ResponseEntity<Flux<DataBuffer>> {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"images.zip\"")
            .body(bizService.execute(fileName,  "DownloadFile"))
    }
}

