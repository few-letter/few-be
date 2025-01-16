tasks.getByName("bootJar") {
    enabled = false
}

tasks.getByName("jar") {
    enabled = true
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-json")
    implementation("org.springframework.data:spring-data-commons")
    api("org.jmolecules.integrations:jmolecules-starter-ddd:${DependencyVersion.JMOLECULES}")

    api("org.springframework.modulith:spring-modulith-events-core")
    implementation("org.springframework.modulith:spring-modulith-events-api")
    implementation("org.springframework.modulith:spring-modulith-starter-jdbc")
}