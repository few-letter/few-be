import org.springframework.boot.gradle.tasks.bundling.BootJar

tasks.withType(BootJar::class.java) {
    loaderImplementation = org.springframework.boot.loader.tools.LoaderImplementation.CLASSIC
}

apply(from = "$rootDir/gradle/image.gradle.kts")

dependencies {
    /** library */
    implementation(project(":library:common"))
    /** domain */
    implementation(project(":domain:generator"))
    implementation(project(":domain:provider"))

    /** starter */
    implementation("org.springframework.boot:spring-boot-starter-actuator")
}

openApi {
    customBootRun {
        jvmArgs.set(listOf("-Dspring.profiles.active=local,new"))
    }
}