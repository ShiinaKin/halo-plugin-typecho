package io.sakurasou.halo.typecho.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import run.halo.app.extension.ConfigMap
import run.halo.app.extension.ExtensionClient

/**
 * @author mashirot
 * 2024/5/9 22:33
 */
@Service
class PATServiceImpl(
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
}