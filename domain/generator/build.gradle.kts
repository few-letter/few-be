tasks.getByName("bootJar") {
    enabled = false
}

tasks.getByName("jar") {
    enabled = true
}

dependencies {
    implementation(project(":library:web"))

    /** jsoup - html parser */
    implementation("org.jsoup:jsoup:1.15.3")

    /** HTTP client **/
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

    /** gson **/
    implementation("com.google.code.gson:gson:${DependencyVersion.GSON}")

    /** jpa */
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
}