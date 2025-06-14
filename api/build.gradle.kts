import org.springframework.boot.gradle.tasks.bundling.BootJar

tasks.withType(BootJar::class.java) {
    loaderImplementation = org.springframework.boot.loader.tools.LoaderImplementation.CLASSIC
}

apply(from = "$rootDir/gradle/image.gradle.kts")

dependencies {
    /** domain */
    implementation(project(":domain:generator"))
}