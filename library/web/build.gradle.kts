tasks.getByName("bootJar") {
    enabled = false
}

tasks.getByName("jar") {
    enabled = true
}

dependencies {
    api(project(":library:security"))

    /** spring starter */
    api("org.springframework.boot:spring-boot-starter-web")
}