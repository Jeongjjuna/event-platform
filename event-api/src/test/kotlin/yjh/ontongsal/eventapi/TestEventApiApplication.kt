package yjh.ontongsal.eventapi

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<EventApiApplication>().with(TestcontainersConfiguration::class).run(*args)
}
