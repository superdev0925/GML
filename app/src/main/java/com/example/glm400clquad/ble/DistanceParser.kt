package com.example.glm400clquad.ble

/**
 * Temporary parser.
 * The GLM400CL protocol must be confirmed with samples at known distances:
 * 1m=?, 2m=?, 3m=?, 4m=?
 */
object DistanceParser {
    fun parse(bytes: ByteArray): String {
        if (bytes.isEmpty()) return "No data"

        // Known sample from nRF Connect: C0-11-00-3A.
        // Until more known-distance samples are collected, show raw packet safely.
        if (bytes.size == 4) {
            val b0 = bytes[0].toInt() and 0xFF
            val b1 = bytes[1].toInt() and 0xFF
            val b2 = bytes[2].toInt() and 0xFF
            val b3 = bytes[3].toInt() and 0xFF
            return "RAW ${bytes.toHexString()}  [$b0,$b1,$b2,$b3]"
        }

        return "RAW ${bytes.toHexString()}"
    }
}
