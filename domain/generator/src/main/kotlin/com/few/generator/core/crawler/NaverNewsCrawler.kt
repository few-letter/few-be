package com.few.generator.core.crawler

import com.few.generator.core.Crawler
import com.few.generator.core.model.ContentSpec
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

@Component
class NaverNewsCrawler(
    @Value("\${crawl.naver.news.max-pages}") var maxPages: Int,
    @Value("\${crawl.naver.news.max-links}") var maxLinks: Int,
) : Crawler {
    private val log = KotlinLogging.logger {}
    private val regexNewsLinks = "https://n\\.news\\.naver\\.com/mnews/article/\\d+/\\d+$"
    private val headers =
        mapOf(
            "User-Agent" to
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36",
        )

    override fun execute(url: String): ContentSpec? {
        log.info { "Start crawling $url" }
        val soup: Document = getSoup(url)

        val title = soup.selectFirst("#title_area > span")
        val date =
            soup.selectFirst(
                "#ct > div.media_end_head.go_trans > div.media_end_head_info.nv_notrans > div.media_end_head_info_datestamp > div:nth-child(1) > span",
            )
        val content = soup.selectFirst("#dic_area")
        val linkElement =
            soup.selectFirst(
                "#ct > div.media_end_head.go_trans > div.media_end_head_info.nv_notrans > div.media_end_head_info_datestamp > a.media_end_head_origin_link",
            )
        val originalLink = linkElement?.attr("href")

        // TODO 원본 데이터 DB 저장으로 변경
        File("soup_content.txt").writeText(soup.outerHtml(), Charsets.UTF_8)

        if (title == null || date == null || content == null) {
            return null
        }

        val dateStr = date.text().trim()
        val dateParts = dateStr.split(" ")

        val dateTime: LocalDateTime =
            if (dateParts.size == 3) {
                val dateOnly = dateParts[0]
                val amPm = dateParts[1]
                val time = dateParts[2]

                val (hour, minute) = time.split(":").map { it.toInt() }
                val adjustedHour =
                    when {
                        amPm == "오후" && hour != 12 -> hour + 12
                        amPm == "오전" && hour == 12 -> 0
                        else -> hour
                    }

                val dateTimeStr = "$dateOnly ${"%02d".format(adjustedHour)}:${"%02d".format(minute)}"
                LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy.MM.dd. HH:mm"))
            } else {
                LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy.MM.dd. HH:mm"))
            }

        return originalLink?.let {
            ContentSpec(
                title = title.text().trim(),
                content = content.text().trim(),
                date = dateTime,
                link = url,
                originalLink = it,
            )
        }
    }

    fun getUrls(
        sid: Int,
        maxPages: Int,
        maxLinks: Int,
    ): List<String> {
        val allLinks = mutableSetOf<String>()

        for (page in 1..maxPages) {
            val url = makeUrl(sid, page)
            val soup = getSoup(url)

            // Regex to match the desired link pattern
            val pattern = Pattern.compile(regexNewsLinks)
            val links =
                soup.select("a[href]").mapNotNull { element ->
                    val href = element.attr("href")
                    if (pattern.matcher(href).matches()) href else null
                }

            allLinks.addAll(links)

            if (allLinks.size >= maxLinks) {
                break
            }
        }

        return allLinks.take(maxLinks).toList()
    }

    fun getUrls(sid: Int): List<String> = getUrls(sid, maxPages, maxLinks)

    private fun getSoup(url: String): Document {
        val connection = Jsoup.connect(url)
        headers.forEach { (key, value) ->
            connection.header(key, value)
        }
        return connection.get()
    }

    private fun makeUrl(
        sid: Int,
        page: Int,
    ) = "https://news.naver.com/main/main.naver?mode=LSD&mid=shm&sid1=$sid#&date=%2000:00:00&page=$page"
}