package com.few.email.provider

interface EmailSendProvider {
    fun sendEmail(
        from: String,
        to: String,
        subject: String,
        message: String,
    ): String
}