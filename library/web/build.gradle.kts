tasks.getByName("bootJar") {
    enabled = false
}

tasks.getByName("jar") {
    enabled = true
}

dependencies {
    api(project(":library:security"))

    /** starter */
    api("org.springframework.boot:spring-boot-starter-web")

    /** apache common */
    implementation("org.apache.commons:commons-lang3:${DependencyVersion.COMMONS_LANG3}")
}