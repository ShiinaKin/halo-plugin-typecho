package io.sakurasou.halo.typecho.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import io.sakurasou.halo.typecho.service.UploadServiceImpl
import io.sakurasou.halo.typecho.util.CompressUtils
import io.sakurasou.halo.typecho.util.PAGE
import io.sakurasou.halo.typecho.util.POST
import io.sakurasou.halo.typecho.util.ParserUtils
import org.apache.commons.io.FileUtils
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import run.halo.app.plugin.ApiVersion
import java.io.File
import java.io.IOException

/**
 * @author mashirot
 * 2024/5/6 17:35
 */
@ApiVersion("io.sakurasou.halo.typecho/v1")
@RestController
@RequestMapping("/typecho")
class TypechoController(
    private val uploadService: UploadServiceImpl
) {

    private val logger = KotlinLogging.logger { this::class.java }

    @PostMapping("/upload")
    fun uploadTypechoFile(@RequestPart("file") file: Mono<FilePart>): Mono<Result<String>> {
        val tempFile = File("temp")
        tempFile.mkdir()
        val uploadFile = File("temp/", System.currentTimeMillis().toString() + "")
        val unCompressedFile = File("temp/", System.currentTimeMillis().toString() + "unz")
        uploadFile.createNewFile()
        return file.flatMap { filePart -> filePart.transferTo(uploadFile) }
            .then(Mono.just(uploadFile))
            .map { upload ->
                try {
                    CompressUtils.unCompress(upload, unCompressedFile)
                    val result = ParserUtils.traverseFolder(unCompressedFile)

                    val pageResult = uploadService.handlePages(result[PAGE]!!)
                    val postResult = uploadService.handlePosts(result[POST]!!)

                    Result.success(
                        "Total Pages: ${pageResult.first} Succeeded: ${pageResult.second}\n" +
                                "Total Posts: ${postResult.first} Succeeded: ${postResult.second}"
                    )
                } catch (e: Exception) {
                    throw e
                }
            }
            .doFinally {
                try {
                    FileUtils.delete(uploadFile)
                    FileUtils.deleteDirectory(unCompressedFile)
                    FileUtils.deleteDirectory(tempFile)
                    logger.info { "unzTemp file deleted: true" }
                } catch (e: IOException) {
                    logger.error { "unzTemp file deleted: false" }
                    throw e
                }
            }
            .onErrorResume {
                logger.error { it }
                Mono.just(Result.failure(it.message ?: "unknown err"))
            }
    }

    data class Result<out T>(val success: Boolean, val failure: Boolean, val data: T?, val msg: String?) {
        companion object {
            fun <T> success(data: T) = Result(success = true, failure = false, data, null)
            fun failure(msg: String) = Result(success = false, failure = true, null, msg)
        }
    }
}
