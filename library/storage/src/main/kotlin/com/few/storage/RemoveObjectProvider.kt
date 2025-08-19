package com.few.storage

interface RemoveObjectProvider {
    fun execute(name: String): Boolean
}