package com.few.web.client

data class SlackBodyProperty(
    val blocks: List<Block>,
)

data class Block(
    val type: String, // section, divider
    val text: Text? = null,
)

data class Text(
    val type: String = "mrkdwn",
    val text: String,
)