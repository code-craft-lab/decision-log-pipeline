import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.spring") version "2.0.20"
    id("org.springframework.boot") version "3.5.8"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "ai.lab.opa"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot WebFlux
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    
    // Spring Kafka
    implementation("org.springframework.kafka:spring-kafka")
    
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    
    // Jackson for JSON
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    
    // Logging
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
    
    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

