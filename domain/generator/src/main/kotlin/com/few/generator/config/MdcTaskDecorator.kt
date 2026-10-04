package com.few.generator.config

import org.slf4j.MDC
import org.springframework.core.task.TaskDecorator

class MdcTaskDecorator : TaskDecorator {
    override fun decorate(runnable: Runnable): Runnable {
        val contextMap = MDC.getCopyOfContextMap()

        return Runnable {
            try {
                if (contextMap != null) {
                    MDC.setContextMap(contextMap)
                } else {
                    MDC.clear()
                }
                runnable.run()
            } finally {
                MDC.clear()
            }
        }
    }
}