package yjh.ontongsal.authapi.shared.web

import jakarta.servlet.ReadListener
import jakarta.servlet.ServletInputStream
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class CachedBodyHttpServletRequest(request: HttpServletRequest) : HttpServletRequestWrapper(request) {

    private val cachedBody: ByteArray = request.inputStream.use { it.readBytes() }

    override fun getInputStream(): ServletInputStream {
        return CachedBodyServletInputStream(cachedBody)
    }

    override fun getReader(): BufferedReader {
        val charset = characterEncoding ?: StandardCharsets.UTF_8.name()
        return BufferedReader(InputStreamReader(ByteArrayInputStream(cachedBody), charset))
    }

    fun getBody(): ByteArray = cachedBody
}

private class CachedBodyServletInputStream(body: ByteArray) : ServletInputStream() {

    private val delegate = ByteArrayInputStream(body)

    override fun isFinished(): Boolean = delegate.available() == 0
    override fun isReady(): Boolean = true
    override fun setReadListener(listener: ReadListener?) = throw UnsupportedOperationException()
    override fun read(): Int = delegate.read()
}