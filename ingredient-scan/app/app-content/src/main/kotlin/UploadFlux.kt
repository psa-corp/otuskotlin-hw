package net.otuskotlin.ingredientscan.app.content

import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.IsContentProvider
import org.springframework.http.codec.multipart.FilePart
import reactor.core.publisher.Flux

suspend fun IsContentProvider.uploadFlux(
    context: IsContext,
    files: Flux<FilePart>,
    prefix: String?
): List<String> {
    return upload(context, files, prefix)
}