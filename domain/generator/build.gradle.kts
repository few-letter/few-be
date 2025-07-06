plugins {
    `java-test-fixtures`
}

tasks.getByName("bootJar") {
    enabled = false
}

tasks.getByName("jar") {
    enabled = true
}

dependencies {
    implementation(project(":library:web"))

    /** starter */
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

    /** swagger */
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${DependencyVersion.SPRINGDOC_OPENAPI}")

    /** mysql */
    implementation("com.mysql:mysql-connector-j")

    /** jsoup - html parser */
    implementation("org.jsoup:jsoup:${DependencyVersion.JSOUP}")

    /** gson **/
    implementation("com.google.code.gson:gson:${DependencyVersion.GSON}")

    /** test */
    testFixturesImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter-kotlin:1.0.15")
}

// generator 모듈 특화 JaCoCo 설정 - 점진적 커버리지 향상
tasks.jacocoTestCoverageVerification {
    isEnabled = false // 초기 단계에서는 비활성화, 커버리지 리포트만 생성
    violationRules {
        // 미래 사용을 위한 설정 (enabled = false로 비활성화 상태)
        rule {
            enabled = false
            element = "CLASS"
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.70".toBigDecimal()
            }
            includes =
                listOf(
                    "com.few.generator.controller.*",
                    "com.few.generator.usecase.*",
                )
            excludes =
                listOf(
                    "*.*Test*",
                    "*.*Fixture*",
                    "*.*TestData*",
                    "*\$*\$*",
                    "*.usecase.input.*",
                    "*.usecase.out.*",
                )
        }
        rule {
            enabled = false
            element = "BUNDLE"
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.30".toBigDecimal()
            }
        }
    }
}