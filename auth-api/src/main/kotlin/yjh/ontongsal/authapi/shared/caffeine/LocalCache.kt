package yjh.ontongsal.authapi.shared.caffeine

import java.time.Duration

enum class LocalCache(
    val namespace: String,
    val ttl: Duration,
    val maximumSize: Long,
) {
    USER_INFO("user_info", Duration.ofHours(5), 10),
    USER_INFOS("user_infos", Duration.ofHours(5), 10),
}