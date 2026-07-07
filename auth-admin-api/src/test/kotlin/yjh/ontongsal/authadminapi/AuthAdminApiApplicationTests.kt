package yjh.ontongsal.authadminapi

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class AuthAdminApiApplicationTests {

    @Test
    fun contextLoads() {
    }

}
