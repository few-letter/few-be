package com.few.generator.core.model

data class GroupContentSpec(
    val topic: String = "",
    val contentSpecs: List<ContentSpec> = listOf(),
    var section: SectionContent = SectionContent(),
) {
    fun toMap(): Map<String, Any> =
        mapOf(
            "topic" to topic,
            "news" to contentSpecs.map { it.toMap() },
            "section" to section.toDict(),
        )

    companion object {
        fun fromMap(data: Map<String, Any>): GroupContentSpec {
            val contentSpecList = (data["news"] as List<Map<String, Any>>).map { ContentSpec.fromMap(it) }
            val sectionData = SectionContent.fromDict(data["section"] as Map<String, Any>? ?: emptyMap())
            return GroupContentSpec(
                topic = data["topic"] as String,
                contentSpecs = contentSpecList,
                section = sectionData,
            )
        }
    }
}

data class SectionContent(
    val title: String = "",
    val contents: List<Content> = listOf(),
) {
    fun toDict(): Map<String, Any> =
        mapOf(
            "title" to title,
            "contents" to contents.map { it.toDict() },
        )

    companion object {
        fun fromDict(data: Map<String, Any>): SectionContent {
            val contentsList =
                (data["contents"] as? List<Map<String, Any>>)?.map { Content.fromDict(it) } ?: emptyList()
            return SectionContent(
                title = data["title"] as? String ?: "",
                contents = contentsList,
            )
        }
    }
}

data class Content(
    val subTitle: String = "",
    val body: String = "",
) {
    fun toDict(): Map<String, Any> =
        mapOf(
            "subTitle" to subTitle,
            "body" to body,
        )

    companion object {
        fun fromDict(data: Map<String, Any>): Content =
            Content(
                subTitle = data["subTitle"] as? String ?: "",
                body = data["body"] as? String ?: "",
            )
    }
}