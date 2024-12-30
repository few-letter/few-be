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
    implementation("org.springframework.boot:spring-boot-starter-json")
}