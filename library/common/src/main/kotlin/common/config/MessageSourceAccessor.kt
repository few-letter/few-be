package common.config

import org.springframework.context.MessageSource
import org.springframework.context.MessageSourceAware
import org.springframework.context.support.MessageSourceAccessor
import org.springframework.stereotype.Component
import java.util.*

@Component
class MessageSourceAccessor : MessageSourceAware {
    override fun setMessageSource(messageSource: MessageSource) {
        messageSourceAccessor =
            MessageSourceAccessor(
                messageSource,
                Locale.getDefault(),
            )
    }

    companion object {
        private var messageSourceAccessor: MessageSourceAccessor? = null

        fun getMessage(code: String?): String =
            messageSourceAccessor!!.getMessage(
                code!!,
            )

        fun getMessage(
            code: String?,
            vararg args: Any?,
        ): String =
            messageSourceAccessor!!.getMessage(
                code!!,
                args,
            )
    }
}