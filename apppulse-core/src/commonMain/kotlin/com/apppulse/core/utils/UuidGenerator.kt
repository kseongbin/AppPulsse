package com.apppulse.core.utils

import kotlin.random.Random

interface UuidGenerator {
    fun random(): String
}

class DefaultUuidGenerator : UuidGenerator {
    override fun random(): String {
        val randomBytes = ByteArray(16)
        Random.nextBytes(randomBytes)
        return randomBytes.joinToString(separator = "") { byte ->
            val value = byte.toInt() and 0xFF
            value.toString(16).padStart(2, '0')
        }
    }
}
