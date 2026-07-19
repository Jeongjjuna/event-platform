package yjh.ontongsal.authapi.shared.response

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.URI


typealias ApiResponseEntity<T> = ResponseEntity<SuccessResponse<T>>

interface ApiController {

    fun <T> ok(
        data: T? = null,
        headers: HttpHeaders = HttpHeaders(),
    ): ApiResponseEntity<T> = ResponseEntity
        .ok()
        .headers(headers)
        .body(
            SuccessResponse(
                code = 200,
                message = HttpStatus.OK.reasonPhrase,
                data = data
            )
        )

    fun <T> created(
        locationUri: URI,
        data: T? = null,
    ): ApiResponseEntity<T> = ResponseEntity
        .created(locationUri)
        .body(
            SuccessResponse(
                code = 200,
                message = HttpStatus.CREATED.reasonPhrase,
                data = data
            )
        )

    fun <T> created(
        id: Long,
        data: T? = null,
    ): ApiResponseEntity<T> {
        val locationUri = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(id)
            .toUri()

        return created(locationUri, data)
    }
}