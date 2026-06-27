package com.few.generator.core.scrapper.timefolio

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TimeEtfScrapperTest :
    FunSpec({
        test("stockName 끝의 회사 접미사를 제거한다") {
            "Sandisk Corp".removeCompanySuffix() shouldBe "Sandisk"
            "Intel Corp".removeCompanySuffix() shouldBe "Intel"
            "Micron Technology Inc".removeCompanySuffix() shouldBe "Micron"
            "Dell Technologies Inc".removeCompanySuffix() shouldBe "Dell"
            "Credo Technology Group Holding Ltd".removeCompanySuffix() shouldBe "Credo"
            "Seagate Technology Holdings PLC".removeCompanySuffix() shouldBe "Seagate"
            "Nebius Group NV".removeCompanySuffix() shouldBe "Nebius"
        }

        test("접미사가 여러 개 겹친 경우 모두 제거한다") {
            "Micron Technology Inc".removeCompanySuffix() shouldBe "Micron"
            "Dell Technologies Inc".removeCompanySuffix() shouldBe "Dell"
            "Credo Technology Group Holding Ltd".removeCompanySuffix() shouldBe "Credo"
            "Seagate Technology Holdings PLC".removeCompanySuffix() shouldBe "Seagate"
        }

        test("접미사가 없는 stockName은 그대로 반환한다") {
            "Apple".removeCompanySuffix() shouldBe "Apple"
            "NVIDIA".removeCompanySuffix() shouldBe "NVIDIA"
            "Microsoft".removeCompanySuffix() shouldBe "Microsoft"
            "Tesla".removeCompanySuffix() shouldBe "Tesla"
        }

        test("접미사가 중간에 포함된 경우 제거하지 않는다") {
            "Corp Solutions".removeCompanySuffix() shouldBe "Corp Solutions"
            "Inc Digital Group".removeCompanySuffix() shouldBe "Inc Digital"
        }
    })