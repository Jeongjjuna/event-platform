package yjh.ontongsal.eventadminapi

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<EventAdminApiApplication>().with(TestcontainersConfiguration::class).run(*args)
}
