package com.few.crm.document

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass
import event.Event
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
        @Link("https://www.planttext.com/")
        @Test
        fun `create dependency diagram`() {
            val modules =
                ApplicationModules.of(
                    "com.few.crm",
                    DescribedPredicate.describe(
                        "ignore event classes",
                        JavaClass.Predicates.assignableTo(Event::class.java),
                    ),
                )
            Documenter(modules)
                .writeIndividualModulesAsPlantUml()

            modules
                .filterNot { it.name == "config" }
                .forEach {
                    val pumlFile = File("build/spring-modulith-docs/module-${it.name}.puml")
                    Allure.addAttachment("${it.name} Module Puml", "text/plain", pumlFile.readText())
                }
        }
    }

    @Nested
    inner class EventDocument {
        @Story("CRM 이벤트 발행 문서")
        @Link("https://thetimetube.herokuapp.com/asciidoc/")
        @Test
        fun `create event document`() {
            val modules =
                ApplicationModules.of(
                    "com.few.crm",
                )
            Documenter(modules)
                .writeModuleCanvases(
                    Documenter.CanvasOptions
                        .defaults()
                        .revealInternals()
                        .revealEmptyLines(),
                )

            modules
                .filterNot { it.name == "config" }
                .forEach {
                    val adocFile = File("build/spring-modulith-docs/module-${it.name}.adoc")
                    Allure.addAttachment("${it.name} Module Adoc", "text/plain", adocFile.readText())
                }
        }
    }
}