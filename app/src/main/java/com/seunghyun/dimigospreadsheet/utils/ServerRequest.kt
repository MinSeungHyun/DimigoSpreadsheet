package com.seunghyun.dimigospreadsheet.utils

import com.seunghyun.dimigospreadsheet.models.Result
import com.seunghyun.dimigospreadsheet.models.ServerCallback
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.nio.charset.StandardCharsets

class ServerRequest {
    companion object {
        fun login(id: String, password: String, callback: ServerCallback) {
            val requestMethod = "POST"
            val url = "https://dev-api.dimigo.in/auth/"
            val headers = HashMap<String, String>()
            headers["Content-Type"] = "application/json"
            val data = "{\"id\": \"$id\", \"password\": \"$password\"}"

            Request(requestMethod, URL(url), headers, data, callback).start()
        }
    }

    private class Request(val requestMethod: String, val url: URL, val headers: HashMap<String, String>, val data: String?, val callback: ServerCallback) : Thread() {
        val result = Result(null, null)
        override fun run() {
            try {
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    doOutput = requestMethod == "POST"
                    requestMethod = this@Request.requestMethod
                    headers.forEach {
                        setRequestProperty(it.key, it.value)
                    }
                    connectTimeout = 2000
                    readTimeout = 2000
                }

                var streamWriter: OutputStreamWriter? = null
                if (data != null) {
                    streamWriter = OutputStreamWriter(connection.outputStream).apply {
                        write(data)
                        flush()
                    }
                }

                result.code = connection.responseCode
                val bufferedReader: BufferedReader = if (result.code == 200) BufferedReader(InputStreamReader(connection.inputStream, StandardCharsets.UTF_8))
                else BufferedReader(InputStreamReader(connection.errorStream, StandardCharsets.UTF_8))
                result.content = bufferedReader.readLine()

                streamWriter?.close()
                bufferedReader.close()
            } catch (e: FileNotFoundException) {
                result.code = 404
            } catch (e: SocketTimeoutException) {
                result.content = "SocketTimeoutException"
            } catch (e: Exception) {
                e.printStackTrace()
                result.content = e.stackTrace.toString()
            }
            callback.onReceive(result)
        }
    }
}