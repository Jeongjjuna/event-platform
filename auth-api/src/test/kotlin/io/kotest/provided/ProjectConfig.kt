package io.kotest.provided

import io.kotest.core.config.AbstractProjectConfig

object ProjectConfig: AbstractProjectConfig() {
    override val extensions = listOf(ApiReportListener())
}