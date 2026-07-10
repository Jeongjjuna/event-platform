package yjh.ontongsal.authapi.shared.web

object LogDepth {
    private val depth = ThreadLocal.withInitial { 0 }

    fun enter(): String = indent().also {
        depth.set(depth.get() + 1)
    }

    fun exit(): String {
        depth.set((depth.get() - 1).coerceAtLeast(0))
        return indent()
    }

    fun clear() = depth.remove()

    private fun indent() = "  ".repeat(depth.get())
}