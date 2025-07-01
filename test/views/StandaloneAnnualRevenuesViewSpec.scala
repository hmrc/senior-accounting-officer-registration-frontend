/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package views

import base.SpecBase
import forms.StandaloneAnnualRevenuesFormProvider
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.StandaloneAnnualRevenuesView

class StandaloneAnnualRevenuesViewSpec extends SpecBase with GuiceOneAppPerSuite {

  val SUT: StandaloneAnnualRevenuesView                  = app.injector.instanceOf[StandaloneAnnualRevenuesView]
  val formProvider: StandaloneAnnualRevenuesFormProvider = app.injector.instanceOf[StandaloneAnnualRevenuesFormProvider]
  given request: Request[_]                              = FakeRequest()
  given Messages                                         = app.injector.instanceOf[MessagesApi].preferred(request)

  "StandaloneAnnualRevenuesView" - {
    "when using a blank form must generate a view" - {
      val form = formProvider()
      val doc  = Jsoup.parse(SUT(form).toString)

      "with the correct heading" in {
        val mainContent = doc.getElementById("main-content")

        val h1 = mainContent.getElementsByTag("h1")
        h1.size() mustBe 1

        h1.get(0).text() mustBe "Does the company have an annual turnover of £200 million or more in the previous financial year?"
      }

      "with the correct radio buttons and nothing selected" in {
        val mainContent = doc.getElementById("main-content")

        val radios = mainContent.getElementsByClass("govuk-radios__item")
        radios.size() mustBe 2

        radios.get(0).text() mustBe "Yes"
        radios.get(1).text() mustBe "No"

        radios.select("input[checked]").size() mustBe 0
      }

      "must show help link" in {
        val mainContent = doc.getElementById("main-content")

        val helpLink = mainContent.getElementsByClass("govuk-link hmrc-report-technical-issue ")
        helpLink.size() mustBe 1
      }
    }

    "when using a form with value -> true must generate a view" - {
      "with the Yes radio button selected" in {
        val form = formProvider().bind(Map("value" -> "true"))
        val doc  = Jsoup.parse(SUT(form).toString)

        val mainContent = doc.getElementById("main-content")

        mainContent.select("input[checked]").size() mustBe 1
        val radios = mainContent.select(".govuk-radios__item input")
        radios.size() mustBe 2

        radios.get(0).attr("value") mustBe "true"
        radios.get(0).hasAttr("checked") mustBe true
        radios.get(1).attr("value") mustBe "false"
        radios.get(1).hasAttr("checked") mustBe false
      }
    }

    "when using a form with value -> false must generate a view" - {
      "with the No radio button selected" in {
        val form = formProvider().bind(Map("value" -> "false"))
        val doc  = Jsoup.parse(SUT(form).toString)

        val mainContent = doc.getElementById("main-content")

        mainContent.select("input[checked]").size() mustBe 1
        val radios = mainContent.select(".govuk-radios__item input")
        radios.size() mustBe 2

        radios.get(0).attr("value") mustBe "true"
        radios.get(0).hasAttr("checked") mustBe false
        radios.get(1).attr("value") mustBe "false"
        radios.get(1).hasAttr("checked") mustBe true
      }
    }

    "when using a validated empty form must generate a view" - {
      val form = formProvider().bind(Map.empty)
      val doc  = Jsoup.parse(SUT(form).toString)

      "with the correct validation error message" in {
        val mainContent = doc.getElementById("main-content")

        val errorSummary = mainContent.getElementsByClass("govuk-error-summary")
        errorSummary.size() mustBe 1
        val errorHeading = errorSummary.select("h2.govuk-error-summary__title")
        errorHeading.size() mustBe 1
        errorHeading.text() mustBe "There is a problem"

        val errorMessages = errorSummary.get(0).select("ul.govuk-list.govuk-error-summary__list li")
        errorMessages.size() mustBe 1
        errorMessages
          .get(0)
          .text() mustBe "Select yes if your company has an annual turnover of £200 million or more in its previous financial year"
      }
    }
  }

}
