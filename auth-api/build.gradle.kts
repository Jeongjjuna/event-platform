plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.spring") version "2.3.21"
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "yjh.ontongsal"
version = "0.0.1-SNAPSHOT"
description = "auth-api"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // spring webmvc
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("tools.jackson.module:jackson-module-kotlin")

    // spring aspectj(aop)
    implementation("org.springframework.boot:spring-boot-starter-aspectj")

    // spring validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // spring security
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")

    // logging
    implementation("io.github.oshai:kotlin-logging-jvm:8.0.01")

    // db
    implementation("org.springframework.boot:spring-boot-h2console")
    runtimeOnly("com.h2database:h2")           // 로컬용
    runtimeOnly("com.mysql:mysql-connector-j") // 운영용

    // spring transaction
    implementation("org.springframework:spring-tx")

    // spring exposed
    implementation("org.jetbrains.exposed:exposed-spring-boot4-starter:1.3.1")
    implementation("org.jetbrains.exposed:exposed-java-time:1.3.1")

    // spring redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // spring cache(caffeine)
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine")

    // test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // kotest
    testImplementation("io.kotest:kotest-runner-junit5:6.2.2")
    testImplementation("io.kotest:kotest-assertions-core:6.2.2")
    testImplementation("io.kotest:kotest-extensions-spring:6.2.2")

    testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
