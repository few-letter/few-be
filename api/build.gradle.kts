import org.springframework.boot.gradle.tasks.bundling.BootJar

tasks.withType(BootJar::class.java) {
    loaderImplementation = org.springframework.boot.loader.tools.LoaderImplementation.CLASSIC
}

apply(from = "$rootDir/gradle/image.gradle.kts")

dependencies {
    /** library */
    implementation(project(":library:common"))
    implementation(project(":library:email"))
    /** domain */
    implementation(project(":domain:generator"))

    /** starter */
    implementation("org.springframework.boot:spring-boot-starter-actuator")
}

openApi {
    customBootRun {
        jvmArgs.set(listOf("-Dspring.profiles.active=local,new"))
    }
}