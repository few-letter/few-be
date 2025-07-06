import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version DependencyVersion.KOTLIN
    kotlin("plugin.spring") version DependencyVersion.KOTLIN
    kotlin("plugin.allopen") version DependencyVersion.KOTLIN
    kotlin("kapt") version DependencyVersion.KOTLIN

    /** spring */
    id("org.springframework.boot") version DependencyVersion.SPRING_BOOT
    id("io.spring.dependency-management") version DependencyVersion.SPRING_DEPENDENCY_MANAGEMENT

    /** springdoc */
    id("org.springdoc.openapi-gradle-plugin") version DependencyVersion.SPRINGDOC_OPENAPI_GRADLE

    /** sonar */
    id("org.sonarqube") version DependencyVersion.SONAR

    /** jacoco */
    jacoco
}

/** apply custom gradle scripts */
apply(from = "$rootDir/gradle/lint.gradle.kts")

/** sonar properties */
sonar {
    properties {
        property("sonar.projectKey", "few-letter_few-be")
        property("sonar.organization", "few-letter")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

java.sourceCompatibility = JavaVersion.VERSION_18

allprojects {
    group = "com.few"

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")

    repositories {
        mavenCentral()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }

    tasks.withType<Wrapper> {
        gradleVersion = "8.5"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

tasks.getByName("bootJar") {
    enabled = false
}

subprojects {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.springdoc.openapi-gradle-plugin")
    apply(plugin = "org.jetbrains.kotlin.plugin.allopen")
    apply(plugin = "org.jetbrains.kotlin.kapt")
    apply(plugin = "jacoco")

    /**
     * https://kotlinlang.org/docs/reference/compiler-plugins.html#spring-support
     * automatically supported annotation
     * @Component, @Async, @Transactional, @Cacheable, @SpringBootTest,
     * @Configuration, @Controller, @RestController, @Service, @Repository.
     * jpa meta-annotations not automatically opened through the default settings of the plugin.spring
     */
    allOpen {
        annotation("jakarta.persistence.Entity")
        annotation("jakarta.persistence.MappedSuperclass")
        annotation("jakarta.persistence.Embeddable")
    }

    dependencyManagement {
        dependencies {
            /**
             * spring boot starter jooq 3.2.5 default jooq version is 3.18.14.
             * But jooq-codegen-gradle need over 3.19.0.
             *  */
            dependency("org.jooq:jooq:${DependencyVersion.JOOQ}")
            imports {
                mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.4")
            }
        }
    }

    dependencies {
        /** starter */
        implementation("org.springframework.boot:spring-boot-starter-validation")
        implementation("org.springframework.boot:spring-boot-starter-web")
        kapt("org.springframework.boot:spring-boot-configuration-processor")

        /** kotlin */
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
        implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

        /** test **/
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("io.mockk:mockk:${DependencyVersion.MOCKK}")

        /** kotest */
        testImplementation("io.kotest:kotest-runner-junit5:${DependencyVersion.KOTEST}")
        testImplementation("io.kotest:kotest-assertions-core:${DependencyVersion.KOTEST}")
        testImplementation("io.kotest:kotest-framework-api:${DependencyVersion.KOTEST}")
        testImplementation("io.kotest:kotest-framework-datatest:${DependencyVersion.KOTEST}")
        testImplementation("io.kotest.extensions:kotest-extensions-spring:${DependencyVersion.KOTEST_EXTENSION}")
        testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${DependencyVersion.COROUTINE_TEST}")
        testImplementation("io.kotest.extensions:kotest-extensions-allure:${DependencyVersion.KOTEST_EXTENSION}")

        /** kotlin logger **/
        implementation("io.github.oshai:kotlin-logging-jvm:${DependencyVersion.KOTLIN_LOGGING}")
    }

    kapt {
        includeCompileClasspath = false
    }

    defaultTasks("bootRun")

    jacoco {
        toolVersion = "0.8.12"
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.test)
        reports {
            xml.required.set(true)
            csv.required.set(false)
            html.required.set(true)
        }
        finalizedBy(tasks.jacocoTestCoverageVerification)
    }

    tasks.jacocoTestCoverageVerification {
        violationRules {
            rule {
                enabled = true
                element = "CLASS"
                limit {
                    counter = "LINE"
                    value = "COVEREDRATIO"
                    minimum = "0.70".toBigDecimal()
                }
                excludes =
                    listOf(
                        "*.config.*",
                        "*.*Application*",
                        "*.domain.*",
                        "*.dto.*",
                        "*.entity.*",
                        "*.exception.*",
                        "*.fixture.*",
                        "*.request.*",
                        "*.response.*",
                        "*.TestConstants*",
                        "*.*Test*",
                        "*.*Fixture*",
                        "*.*TestData*",
                    )
            }
        }
    }

    tasks.test {
        finalizedBy(tasks.jacocoTestReport)
    }
}