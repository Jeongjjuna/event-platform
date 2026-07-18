package yjh.ontongsal.authapi.shared.web

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cors")
data class CorsProperties(
    var allowedOrigins: List<String> = emptyList()
)
