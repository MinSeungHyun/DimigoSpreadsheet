package com.seunghyun.dimigospreadsheet.utils

import android.util.Base64
import java.nio.charset.StandardCharsets

class JWTDecoder {
    companion object {
        fun getHeader(jwt: String): String {
            val split = jwt.split(".")
            return getJson(split[0])
        }

        fun getBody(jwt: String): String {
            val split = jwt.split(".")
            return getJson(split[1])
        }

        private fun getJson(strEncoded: String): String {
            val decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE)
            return String(decodedBytes, StandardCharsets.UTF_8)
        }
    }
}