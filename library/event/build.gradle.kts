tasks.getByName("bootJar") {
    enabled = false
}

tasks.getByName("jar") {
    enabled = true
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-json")
    api("org.jmolecules.integrations:jmolecules-starter-ddd:${DependencyVersion.JMOLECULES}")
}