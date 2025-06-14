tasks.getByName("bootJar") {
    enabled = false
}

tasks.getByName("jar") {
    enabled = true
}

plugins {
    /** test  fixtures */
    id("java-test-fixtures")
}

dependencies {
    api(project(":library:security"))

    /** spring starter */
    api("org.springframework.boot:spring-boot-starter-web")

    /** swagger & restdocs */
    api("org.springframework.restdocs:spring-restdocs-webtestclient")
    api("org.springframework.restdocs:spring-restdocs-mockmvc")
    api("org.springdoc:springdoc-openapi-ui:${DependencyVersion.SPRINGDOC}")
    api("com.epages:restdocs-api-spec-mockmvc:${DependencyVersion.EPAGES_REST_DOCS_API_SPEC}")
}