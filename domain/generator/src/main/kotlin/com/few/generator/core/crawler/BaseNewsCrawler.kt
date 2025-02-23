package com.few.generator.core.crawler

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import java.lang.Thread.sleep

@Component
abstract class BaseNewsCrawler(
    private val generatorRestTemplate: RestTemplate,
    private val retryCount: Int = 3,
    private val sleepTime: Long = 1000,
) {
    protected val logger = KotlinLogging.logger {}

    fun getRequest(url: String): String? {
        repeat(retryCount) {
            try {
                val response = generatorRestTemplate.getForEntity<String>(url)
                sleep(sleepTime)
                return response.body
            } catch (e: HttpClientErrorException) {
                logger.error { "Request failed: ${e.message}" }
            } catch (e: ResourceAccessException) {
                logger.error { "Request timeout or connection issue: ${e.message}" }
            } catch (e: Exception) {
                logger.error { "Unexpected error: ${e.message}" }
            }
        }
        return null
    }

    protected open fun getSoup(url: String): Document? {
        val html = getRequest(url)
        return try {
            html?.let { Jsoup.parse(it) }
        } catch (e: Exception) {
            logger.error { "Failed to parse HTML: ${e.message}" }
            null
        }
    }

    abstract fun getNewsUrls(
        topic: String,
        limit: Int,
    ): List<String>

    abstract fun getAvailableTopics(): List<String>
}