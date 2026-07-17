package io.kotest.provided

data class ScenarioReport(
    val describe: String,
    val context: String,
    val it: String,
    val status: Int?,
    val duration: Long,
    val request: Any?,
    val response: Any?,
)