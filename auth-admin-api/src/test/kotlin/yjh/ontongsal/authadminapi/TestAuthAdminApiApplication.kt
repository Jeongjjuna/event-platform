package yjh.ontongsal.authadminapi

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<AuthAdminApiApplication>().with(TestcontainersConfiguration::class).run(*args)
}
