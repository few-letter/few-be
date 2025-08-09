tasks.getByName("bootJar") {
    enabled = false
}

tasks.getByName("jar") {
    enabled = true
}

dependencies {
    implementation(project(":library:web"))
    implementation(project(":library:common"))

    /** starter **/
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    /** swagger **/
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${DependencyVersion.SPRINGDOC_OPENAPI}")

    /** mysql **/
    implementation("com.mysql:mysql-connector-j")

    /** gson **/
    implementation("com.google.code.gson:gson:${DependencyVersion.GSON}")
}