package com.few.crm.document

import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaModifier
import com.tngtech.archunit.core.importer.ClassFileImporter
import event.Event
import event.EventDetails
import io.qameta.allure.Allure
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import io.qameta.allure.Link
import io.qameta.allure.Story
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules
import org.springframework.modulith.docs.Documenter
import java.io.File

@Epic("V2.0 CRM")
@Feature("Document")
class CrmDocument {
    @Nested
    inner class DependencyDiagram {
        @Story("CRM 모듈 의존성 다이어그램")
        @Link("https://thetimetube.herokuapp.com/asciidoc/")
        @Test
        fun `create dependency diagram`() {
            val modules = ApplicationModules.of("com.few.crm")
            Documenter(modules)
                .writeDocumentation()
                .writeIndividualModulesAsPlantUml()

            modules
                .filterNot { it.name == "config" }
                .forEach {
                    val outputFile = File("build/spring-modulith-docs/module-${it.name}.adoc")
                    Allure.addAttachment("${it.name} Module", "text/plain", outputFile.readText())
                }
        }
    }

    @Nested
    inner class EventDocument {
        @Story("CRM 이벤트 발행 문서")
        @Test
        fun `create event document`() {
            val classes = ClassFileImporter().importPackages("com.few.crm")
            val eventClasses =
                classes
                    .stream()
                    .filter { it.isAssignableTo(Event::class.java) }
                    .filter { it.isAnonymousClass.not() }
                    .filter { it.isInnerClass.not() }
                    .filter { it.isLocalClass.not() }
                    .filter { it.modifiers.contains(JavaModifier.ABSTRACT).not() }
                    .toList()

            val notQualifiedEventClasses = mutableListOf<JavaClass>()
            val logBuilder = StringBuilder()

            eventClasses.forEach { event ->
                if (event.isAnnotatedWith(EventDetails::class.java)) {
                    val eventDetails = event.getAnnotationOfType(EventDetails::class.java)
                    val publishedLocations = eventDetails.publishedLocations
                    if (publishedLocations.isEmpty()) {
                        notQualifiedEventClasses.add(event)
                    } else {
                        publishedLocations
                            .filter { it != (event.packageName + "." + event.simpleName) }
                            .forEach { publishedLocation ->
                                event.directDependenciesToSelf
                                    .find {
                                        it.originClass.fullName == publishedLocation
                                    }?.let {
                                        logBuilder.appendLine("* ${it.originClass.fullName}")
                                    } ?: run {
                                    notQualifiedEventClasses.add(event)
                                }
                            }
                    }
                } else {
                    notQualifiedEventClasses.add(event)
                }
            }

            if (notQualifiedEventClasses.isNotEmpty()) {
                logBuilder.appendLine("\n== Not Qualified Event Classes")
                logBuilder.appendLine("_The following event classes are not annotated with @EventDetails:_")
                notQualifiedEventClasses.forEach {
                    logBuilder.appendLine("* ${it.fullName}")
                }
                throw IllegalStateException(logBuilder.toString())
            }

            val outputFile = File("build/event-docs/event-published-document.adoc")
            if (outputFile.exists()) {
                outputFile.delete()
            }
            outputFile.parentFile.mkdirs()
            val adocContent = StringBuilder()

            adocContent.appendLine("[%autowidth.stretch, cols=\"h,a\"]")
            adocContent.appendLine("|===")
            adocContent.appendLine("|Event Class | Published Locations")

            eventClasses.forEach { event ->
                val eventDetails = event.getAnnotationOfType(EventDetails::class.java)
                val publishedLocations = eventDetails.publishedLocations

                adocContent.appendLine("|`${event.simpleName}`")
                adocContent.appendLine("|")
                adocContent.appendLine(
                    publishedLocations.joinToString("\n") { "* `$it`" },
                )
            }

            adocContent.appendLine("|===")

            outputFile.writeText(adocContent.toString())

            Allure.addAttachment("Event Document", "text/plain", adocContent.toString())
            println("Event document generated at: ${outputFile.absolutePath}")
        }
    }
}