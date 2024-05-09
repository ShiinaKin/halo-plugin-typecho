package io.sakurasou.halo.typecho.util

import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.ArchiveException
import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.apache.commons.io.IOUtils
import java.io.*

/**
 * @author mashirot
 * 2024/5/6 21:41
 */
object CompressUtils {
    @Throws(IOException::class, ArchiveException::class)
    fun unCompress(input: File, output: File) {
        val archiveFactory = ArchiveStreamFactory("utf-8")
        BufferedInputStream(FileInputStream(input)).use { bis ->
            val archiveInputStream = archiveFactory.createArchiveInputStream<ArchiveInputStream<out ArchiveEntry>>(bis)
            var archiveEntry = archiveInputStream.nextEntry
            while (archiveEntry != null) {
                val outputFile = File(output, archiveEntry.name)
                if (archiveEntry.isDirectory && !outputFile.exists()) {
                    outputFile.mkdirs()
                } else {
                    if (!outputFile.parentFile.exists()) {
                        outputFile.parentFile.mkdirs()
                    }
                    FileOutputStream(outputFile).use { outputStream ->
                        IOUtils.copy(archiveInputStream, outputStream)
                    }
                }
                archiveEntry = archiveInputStream.nextEntry
            }
        }
    }
}
