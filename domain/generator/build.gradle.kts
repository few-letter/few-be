tasks.getByName("bootJar") {
    enabled = false
}

tasks.getByName("jar") {
    enabled = true
}

plugins {
    kotlin("plugin.jpa") version DependencyVersion.JPA_PLUGIN
}

dependencies {
    implementation(project(":library:web"))
    implementation(project(":library:common"))

    /** starter */
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

    /** swagger */
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${DependencyVersion.SPRINGDOC_OPENAPI}")

    /** mysql */
    implementation("com.mysql:mysql-connector-j")

    /** jsoup - html parser */
    implementation("org.jsoup:jsoup:${DependencyVersion.JSOUP}")

    /** gson **/
    implementation("com.google.code.gson:gson:${DependencyVersion.GSON}")

    /** okhttp **/
    implementation("com.squareup.okhttp3:okhttp:${DependencyVersion.OKHTTP}")

    /** okhttp - Brotli compression **/
    implementation("com.squareup.okhttp3:okhttp-brotli:${DependencyVersion.OKHTTP}")
}