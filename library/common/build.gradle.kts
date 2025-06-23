tasks.getByName("bootJar") {
    enabled = false
}

tasks.getByName("jar") {
    enabled = true
}

dependencies {
    /** apache common */
    implementation("org.apache.commons:commons-lang3:${DependencyVersion.COMMONS_LANG3}")

    /** swagger */
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${DependencyVersion.SPRINGDOC_OPENAPI}")
}