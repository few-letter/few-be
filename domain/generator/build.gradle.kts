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

    /** mysql */
    implementation("com.mysql:mysql-connector-j")

    /** jsoup - html parser */
    implementation("org.jsoup:jsoup:${DependencyVersion.JSOUP}")

    /** gson **/
    implementation("com.google.code.gson:gson:${DependencyVersion.GSON}")
}