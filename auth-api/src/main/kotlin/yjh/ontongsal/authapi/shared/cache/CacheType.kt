package yjh.ontongsal.authapi.shared.cache

import java.time.Duration

/**
 * 애플리케이션 캐시의 단일 관리 지점.
 * Caffeine(로컬), Redis(글로벌), 어노테이션 기반 구현체 모두 여기서 캐시 이름을 가져다 쓴다.
 */
enum class CacheType(
    val cacheName: String,
    val localTtl: Duration, // Caffeine 전용 설정
    val localMaximumSize: Long, // Caffeine 전용 설정
    val globalTtl: Duration, // Redis 전용 설정
) {
    USER_INFO(Name.USER_INFO, Duration.ofHours(5), 10, Duration.ofMinutes(30)),
    USER_INFOS(Name.USER_INFOS, Duration.ofHours(5), 10, Duration.ofMinutes(30));

    /**
     * 컴파일 타임 상수. 어노테이션(@Cacheable 등)과 런타임 조회(from)의 공통 진입점이다.
     * const는 컴파일 시 인라인되므로 위 enum 상수 정의에서도 참조할 수 있다.
     */
    object Name {
        const val USER_INFO = "user_info"
        const val USER_INFOS = "user_infos"
    }

    companion object {
        fun from(cacheName: String): CacheType {
            val cacheType = entries.find { it.cacheName == cacheName }
            return requireNotNull(cacheType) {
                "CacheType에 등록되지 않은 캐시 이름입니다. cacheName=$cacheName"
            }
        }
    }
}
