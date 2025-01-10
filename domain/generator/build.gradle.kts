tasks.getByName("bootJar") {
    enabled = false
}

tasks.getByName("jar") {
    enabled = true
}

dependencyManagement {
    imports {
    }
}

dependencies {
    implementation(project(":library:web"))

    /** jsoup - html parser */
    implementation("org.jsoup:jsoup:1.15.3")

    /** coroutines **/
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")

    /** HTTP client **/
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

    /** gson **/
    implementation("com.google.code.gson:gson:2.10.1")
}