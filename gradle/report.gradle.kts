import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.testing.Test

tasks.named<Test>("test") {
    useJUnitPlatform()
    systemProperty("allure.results.directory", "$projectDir/build/allure-results")
}

tasks.register<Copy>("allureReport") {
    group = "report"
    from("$projectDir/build/allure-results")
    into("$rootDir/allure-results")
}