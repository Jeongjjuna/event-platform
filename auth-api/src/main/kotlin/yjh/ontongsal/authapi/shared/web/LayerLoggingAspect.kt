package yjh.ontongsal.authapi.shared.web

import io.github.oshai.kotlinlogging.KotlinLogging
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Aspect
@Component
@Profile("local", "integration-test")
class LayerLoggingAspect {

    @Pointcut("execution(* yjh.ontongsal.authapi..presentation..*(..))")
    fun presentation() {}

    @Pointcut("execution(* yjh.ontongsal.authapi..application..*(..))")
    fun application() {}

    // 클래스에 @Service가 붙어 있으면 비즈니스 로직, 아니면(@Component) 구현 로직으로 구분한다.
    @Pointcut("@within(org.springframework.stereotype.Service)")
    fun serviceStereotype() {}

    @Pointcut("execution(* yjh.ontongsal.authapi..infrastructure..*(..))")
    fun infrastructure() {}

    @Around("presentation()")
    fun logPresentation(jp: ProceedingJoinPoint) = logLayer(jp, "WEB")

    @Around("application() && serviceStereotype()")
    fun logAppBusiness(jp: ProceedingJoinPoint) = logLayer(jp, "APP(B)")

    @Around("application() && !serviceStereotype()")
    fun logAppImplement(jp: ProceedingJoinPoint) = logLayer(jp, "APP(I)")

    @Around("infrastructure()")
    fun logInfrastructure(jp: ProceedingJoinPoint) = logLayer(jp, "INFRA")

    private fun logLayer(jp: ProceedingJoinPoint, layer: String): Any? {
        val sig = "${jp.signature.declaringType.simpleName}.${jp.signature.name}"

        val enterIndent = LogDepth.enter()
        log.debug { "$enterIndent[$layer] --> $sig" }
        val start = System.nanoTime()

        return try {
            val result = jp.proceed()
            val indent = LogDepth.exit()
            log.debug { "$indent[$layer] <-- $sig (${elapsedMs(start)}ms)" }
            result
        } catch (e: Throwable) {
            val indent = LogDepth.exit()
            log.debug { "$indent[$layer] <-X $sig (${elapsedMs(start)}ms) ${e.javaClass.simpleName}: ${e.message}" }
            throw e
        }
    }

    private fun elapsedMs(startNanos: Long): Long =
        (System.nanoTime() - startNanos) / 1_000_000
}