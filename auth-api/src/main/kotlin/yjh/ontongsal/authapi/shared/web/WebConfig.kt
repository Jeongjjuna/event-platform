package yjh.ontongsal.authapi.shared.web

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@EnableConfigurationProperties(CorsProperties::class)
@Configuration
class WebConfig(
    private val corsProperties: CorsProperties
) : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry
            // 어떤 URL에 대해 CORS를 적용할지
            // "/**" = 모든 API
            .addMapping("/**")

            // 요청을 허용할 Origin(브라우저 주소)
            // 로컬 개발에서는 Nuxt 개발 서버만 허용
            // 운영에서는 https://example.com 같은 실제 도메인만 허용
            .allowedOrigins(*corsProperties.allowedOrigins.toTypedArray())

            // 브라우저가 사용할 수 있는 HTTP Method
            .allowedMethods(
                "GET",      // 조회
                "POST",     // 생성
                "PUT",      // 전체 수정
                "PATCH",    // 일부 수정
                "DELETE",   // 삭제
                "OPTIONS"   // Preflight 요청
            )

            // 클라이언트가 보낼 Request Header 허용
            // "*" = 모든 Header 허용
            // Authorization, Content-Type 등이 포함됨
            .allowedHeaders("*")

            // 쿠키(JSESSIONID), Authorization 등 Credential 전송 허용
            // true인 경우 allowedOrigins("*")는 사용할 수 없음
            .allowCredentials(true)

            // 브라우저(JavaScript)가 읽을 수 있도록 노출할 Response Header
            // 기본적으로 Content-Type 정도만 읽을 수 있기 때문에
            // 커스텀 Header는 명시적으로 노출해야 함
            .exposedHeaders("X-Service-Name")
    }
}