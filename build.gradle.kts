import org.hidetake.gradle.swagger.generator.GenerateSwaggerUI
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version DependencyVersion.KOTLIN
    kotlin("plugin.spring") version DependencyVersion.KOTLIN
    kotlin("plugin.allopen") version DependencyVersion.KOTLIN
    kotlin("kapt") version DependencyVersion.KOTLIN

    /** spring */
    id("org.springframework.boot") version DependencyVersion.SPRING_BOOT
    id("io.spring.dependency-management") version DependencyVersion.SPRING_DEPENDENCY_MANAGEMENT

    /** docs */
    id("org.asciidoctor.jvm.convert") version DependencyVersion.ASCIIDOCTOR
    id("com.epages.restdocs-api-spec") version DependencyVersion.EPAGES_REST_DOCS_API_SPEC
    id("org.hidetake.swagger.generator") version DependencyVersion.SWAGGER_GENERATOR

    /** sonar */
    id("org.sonarqube") version DependencyVersion.SONAR
}

java.sourceCompatibility = JavaVersion.VERSION_18

allprojects {
    group = "com.few"

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")

    repositories {
        mavenCentral()
        maven("https://maven.vaadin.com/vaadin-addons")
    }

    val ktlint by configurations.creating

    dependencies {
        ktlint("com.pinterest.ktlint:ktlint-cli:${DependencyVersion.PINTEREST_KTLINT}") {
            attributes {
                attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
            }
        }
    }

    val ktlintCheck by tasks.registering(JavaExec::class) {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Check Kotlin code style"
        classpath = ktlint
        mainClass.set("com.pinterest.ktlint.Main")
        args(
            "**/src/**/*.kt",
            "**.kts",
            "!**/build/**",
        )
    }

    tasks.check {
        dependsOn(ktlintCheck)
    }

    tasks.register<JavaExec>("ktlintFormat") {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Check Kotlin code style and format"
        classpath = ktlint
        mainClass.set("com.pinterest.ktlint.Main")
        jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
        args(
            "-F",
            "**/src/**/*.kt",
            "**.kts",
            "!**/build/**",
        )
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

    sourceSets {
        main {
            java {
                val mainDir = "src/main/kotlin"
                val jooqDir = "src/generated"
                srcDirs(mainDir, jooqDir)
            }
        }
    }
}

tasks.getByName("bootJar") {
    enabled = false
}

subprojects {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jetbrains.kotlin.plugin.allopen")
    apply(plugin = "org.jetbrains.kotlin.kapt")
    apply(plugin = "org.hidetake.swagger.generator")
    apply(plugin = "org.asciidoctor.jvm.convert")
    apply(plugin = "com.epages.restdocs-api-spec")

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
                mavenBom("org.springframework.modulith:spring-modulith-bom:${DependencyVersion.SPRING_MODULITH}")
                mavenBom("com.vaadin:vaadin-bom:${DependencyVersion.VAADIN}")
                mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.4")
            }
        }
    }

    dependencies {
        /** spring starter */
        implementation("org.springframework.boot:spring-boot-starter-validation")
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("org.springframework.modulith:spring-modulith-starter-core")
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
        testImplementation("com.tngtech.archunit:archunit-junit5:${DependencyVersion.ARCH_UNIT_JUNIT5}")
        testImplementation("org.springframework.modulith:spring-modulith-starter-test")
        testImplementation("io.qameta.allure:allure-junit5:${DependencyVersion.ALLURE_JUNIT5}")

        /** kotest */
        testImplementation("io.kotest:kotest-runner-junit5:${DependencyVersion.KOTEST}")
        testImplementation("io.kotest:kotest-assertions-core:${DependencyVersion.KOTEST}")
        testImplementation("io.kotest:kotest-framework-api:${DependencyVersion.KOTEST}")
        testImplementation("io.kotest.extensions:kotest-extensions-spring:${DependencyVersion.KOTEST_EXTENSION}")
        testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${DependencyVersion.COROUTINE_TEST}")
        testImplementation("io.kotest.extensions:kotest-extensions-allure:${DependencyVersion.KOTEST_EXTENSION}")

        /** Kotlin Logger **/
        implementation("io.github.oshai:kotlin-logging-jvm:${DependencyVersion.KOTLIN_LOGGING}")

        /** apache common */
        implementation("org.apache.commons:commons-lang3:${DependencyVersion.COMMONS_LANG3}")

        /** swagger ui */
        swaggerUI("org.webjars:swagger-ui:${DependencyVersion.SWAGGER_UI}")
    }

    kapt {
        includeCompileClasspath = false
    }

    tasks {
        test {
            useJUnitPlatform()
            systemProperty("allure.results.directory", "$projectDir/build/allure-results")
        }
    }

    /** server url */
    val serverUrl =
        project.hasProperty("serverUrl").let {
            if (it) {
                project.property("serverUrl") as String
            } else {
                "http://localhost:8080"
            }
        }

    /** convert snippet to swagger */
    openapi3 {
        this.setServer(serverUrl)
        title = project.name
        version = project.version.toString()
        format = "yaml"
        snippetsDirectory = "build/generated-snippets/"
        outputDirectory = "src/main/resources/static"
        outputFileNamePrefix = "openapi3"
    }

    /** convert snippet to postman */
    postman {
        title = project.name
        version = project.version.toString()
        baseUrl = serverUrl
        outputDirectory = "src/main/resources/static"
        outputFileNamePrefix = "postman"
    }

    /** generate swagger ui */
    swaggerSources {
        register(project.name) {
            setInputFile(file("$projectDir/src/main/resources/static/openapi3.yaml"))
        }
    }

    /**
     * generate static swagger ui <br/>
     * need snippet to generate swagger ui
     * */
    tasks.register("generateStaticSwaggerUI", Copy::class) {
        val name = project.name
        val generateSwaggerUITask = "generateSwaggerUI${name.first().uppercase() + name.substring(1)}"
        dependsOn(generateSwaggerUITask)

        val generateSwaggerUISampleTask = tasks.named(generateSwaggerUITask, GenerateSwaggerUI::class).get()
        from(generateSwaggerUISampleTask.outputDir)
        into("$projectDir/src/main/resources/static/docs/${project.name}/swagger-ui")
    }

    tasks.register("allureReport", Copy::class) {
        group = "documentation"

        from("$projectDir/build/allure-results")
        into("$rootDir/allure-results")
    }

    defaultTasks("bootRun")
}

/** git hooks */
tasks.register("gitExecutableHooks") {
    doLast {
        Runtime.getRuntime().exec("chmod -R +x .git/hooks/").waitFor()
    }
}

tasks.register<Copy>("installGitHooks") {
    val scriptDir = "${rootProject.rootDir}/scripts"
    from("$scriptDir/pre-commit")
    into("${rootProject.rootDir}/.git/hooks")
}

tasks.named("gitExecutableHooks").configure {
    dependsOn("installGitHooks")
}

tasks.named("clean").configure {
    dependsOn("gitExecutableHooks")
}

sonar {
    properties {
        property("sonar.projectKey", "few-letter_few-be")
        property("sonar.organization", "few-letter")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}