tasks.getByName("bootJar") {
    enabled = false
}

tasks.getByName("jar") {
    enabled = true
}

dependencies {
    implementation(project(":library:common"))
    implementation(project(":library:web"))

    /** jpa */
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    /** jsoup - html parser */
    implementation("org.jsoup:jsoup:${DependencyVersion.JSOUP}")

    /** gson **/
    implementation("com.google.code.gson:gson:${DependencyVersion.GSON}")

    /** HTTP client **/
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
}