tasks.getByName("bootJar") {
    enabled = false
}

tasks.getByName("jar") {
    enabled = true
}

dependencies {
    implementation(project(":library:web"))

    /** jsoup - html parser */
    implementation("org.jsoup:jsoup:${DependencyVersion.JSOUP}")

    /** HTTP client **/
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

    /** gson **/
    implementation("com.google.code.gson:gson:${DependencyVersion.GSON}")

    /** jpa */
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    /** Coroutines for Spring */
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${DependencyVersion.COROUTINES_SPRING}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${DependencyVersion.COROUTINES_SPRING}")
}