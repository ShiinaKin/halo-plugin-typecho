package io.sakurasou.halo.typecho.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import run.halo.app.extension.ConfigMap
import run.halo.app.extension.ExtensionClient
import java.time.ZoneId

/**
 * @author mashirot
 * 2024/5/9 22:33
 */
@Service
class ConfigServiceImpl(
    private val extensionClient: ExtensionClient,
) {
    private val logger = KotlinLogging.logger { this::class.java }
    private val jsonMapper = ObjectMapper().registerModules(JavaTimeModule(), kotlinModule())

    fun getPAT(): String {
        val configMap = extensionClient.fetch(ConfigMap::class.java, "plugin-typecho-configmap").get()
        val basicGroup = jsonMapper.readValue(configMap.data["basic"], Map::class.java)
        val pat = basicGroup["pat"] as String?
        return pat?.let { it.ifBlank { throw RuntimeException("No PAT") } }
            ?: throw RuntimeException("No PAT")
    }

    fun getTimeZone(): ZoneId {
        val configMap = extensionClient.fetch(ConfigMap::class.java, "plugin-typecho-configmap").get()
        val basicGroup = jsonMapper.readValue(configMap.data["basic"], Map::class.java)
        val timeZone = basicGroup["timeZone"] as String?
        return timeZone?.let {
            it.ifBlank { throw RuntimeException("No TimeZone") }
            runCatching {
                ZoneId.of(it)
            }.getOrElse { throw RuntimeException("Wrong TimeZone") }
        } ?: throw RuntimeException("No TimeZone")
    }
}