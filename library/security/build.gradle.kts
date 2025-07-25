

tasks.getByName("bootJar") {
    enabled = false
}

tasks.getByName("jar") {
    enabled = true
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-security")

    /** apache common */
    implementation("org.apache.commons:commons-lang3:${DependencyVersion.COMMONS_LANG3}")

    /** jwt */
    implementation("io.jsonwebtoken:jjwt-api:${DependencyVersion.JWT}")
    implementation("io.jsonwebtoken:jjwt-impl:${DependencyVersion.JWT}")
    implementation("io.jsonwebtoken:jjwt-jackson:${DependencyVersion.JWT}")

    /** test */
    api("org.springframework.security:spring-security-test")
}