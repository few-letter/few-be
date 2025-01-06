package com.few.crm.email.service

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import org.springframework.stereotype.Service

@Service
class HtmlValidator {
    fun isValidHtml(input: String): Boolean =
        try {
            Jsoup.parse(input, "", Parser.htmlParser())
            true
        } catch (e: Exception) {
            false
        }

    fun prettyPrintHtml(input: String): String? =
        try {
            val document: Document = Jsoup.parse(input, "", Parser.htmlParser())
            document.normalise()
            document.outerHtml()
        } catch (e: Exception) {
            null
        }

    fun extractVariables(input: String): List<String> {
        val document: Document = Jsoup.parse(input, "", Parser.htmlParser())
        val variables = mutableSetOf<String>() // 중복 제거를 위해 Set 사용

        document.allElements.forEach { element ->
            element.attributes().forEach { attr ->
                val value = attr.value
                val regex = """\$\{([a-zA-Z0-9_.]+)}""".toRegex()
                regex.findAll(value).forEach { matchResult ->
                    variables.add(matchResult.groupValues[1])
                }
            }
        }

        return variables.toList()
    }
}