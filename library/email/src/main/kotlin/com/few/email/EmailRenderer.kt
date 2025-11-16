package com.few.email

interface EmailRenderer<T> {
    fun render(model: T): String
}