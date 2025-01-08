tasks.getByName("bootJar") {
    enabled = false
}

tasks.getByName("jar") {
    enabled = true
}

plugins {
    id("com.vaadin") version DependencyVersion.VAADIN
}

dependencies {
    implementation(project(":library:web"))
    implementation(project(":library:email"))
    implementation(project(":library:event"))
    testImplementation(testFixtures(project(":library:event")))

    /** jsoup - html parser */
    implementation("org.jsoup:jsoup:1.15.3")

    /** jpa */
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    /** aws - sqs */
    implementation("io.awspring.cloud:spring-cloud-aws-starter-sqs:${DependencyVersion.AWS_SQS}")

    implementation("software.amazon.awssdk:scheduler:2.29.45")

    /** vaadin */
    implementation("com.vaadin:vaadin-spring-boot-starter")
}

vaadin {
    pnpmEnable = true
    productionMode = true
}

tasks.named("bootJar") {
    dependsOn("vaadinBuildFrontend")
}

tasks.withType(Test::class.java) {
    useJUnitPlatform()
    systemProperty("allure.results.directory", "$projectDir/build/allure-results")
}