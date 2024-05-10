package io.sakurasou.halo.typecho.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import io.sakurasou.halo.typecho.service.UploadServiceImpl
import io.sakurasou.halo.typecho.util.CompressUtils
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
    @Throws(IOException::class)
    fun uploadTypechoFile(@RequestPart("file") file: Mono<FilePart>): Mono<String> {
        val uploadFile = File("temp/", System.currentTimeMillis().toString() + "")
        val unCompressedFile = File("temp/", System.currentTimeMillis().toString() + "unz")
        uploadFile.createNewFile()
        return file.flatMap { filePart -> filePart.transferTo(uploadFile) }
            .then(Mono.just(uploadFile))
            .map { upload ->
                try {
                    CompressUtils.unCompress(upload, unCompressedFile)
                    val result = ParserUtils.traverseFolder(unCompressedFile)

                    // page singlepages.content.halo.run/{}
                    // TODO insert page

                    // post
                    uploadService.handlePosts(result[POST]!!)
                } catch (e: Exception) {
                    throw RuntimeException(e)
                }
                "ok"
            }
            .doFinally {
                try {
                    FileUtils.delete(uploadFile)
                    FileUtils.deleteDirectory(unCompressedFile)
                    logger.info { "unzTemp file deleted: true" }
                } catch (e: IOException) {
                    logger.info { "unzTemp file deleted: false" }
                    throw RuntimeException(e)
                }
            }
            .onErrorResume {
                it.printStackTrace()
                // TODO return err
                Mono.just<String>(it.toString())
            }
    }
}
