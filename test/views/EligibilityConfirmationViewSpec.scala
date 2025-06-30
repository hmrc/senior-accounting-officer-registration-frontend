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
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.EligibilityConfirmationView

import scala.jdk.CollectionConverters.*

class EligibilityConfirmationViewSpec extends SpecBase with GuiceOneAppPerSuite {

  val SUT: EligibilityConfirmationView = app.injector.instanceOf[EligibilityConfirmationView]
  given request: Request[_]            = FakeRequest()
  given Messages                       = app.injector.instanceOf[MessagesApi].preferred(request)

  "EligibilityConfirmationView" - {
    "must generate a view" - {
      val doc = Jsoup.parse(SUT().toString)

      "with the correct heading" in {
        val mainContent = doc.getElementById("main-content")

        val confirmationPanel = mainContent.getElementsByClass("govuk-panel govuk-panel--confirmation")
        confirmationPanel.size() mustBe 1
        val h1 = confirmationPanel.get(0).getElementsByTag("h1")
        h1.size() mustBe 1

        h1.get(0).text() mustBe "Eligibility complete"

        val p1 = confirmationPanel.get(0).select("div.govuk-panel__body")
        p1.size() mustBe 1

        p1.text() mustBe "You can now register"
      }

      "with the correct paragraphs" in {
        val mainContent = doc.getElementById("main-content")

        val paragraphs = mainContent.getElementsByTag("p")
        paragraphs.size() mustBe 2
        List.from(paragraphs.iterator().asScala).foreach(p => p.attr("class") mustBe "govuk-body")

        paragraphs
          .get(0)
          .text() mustBe "You now need to sign in with an organisation Government Gateway user ID associated with the filing member."
        paragraphs.get(1).text() mustBe "Is this page not working properly? (opens in new tab)"
      }

      "must have a continue button" in {
        val mainContent = doc.getElementById("main-content")

        mainContent.getElementById("submit").text() mustBe "Continue"
      }

      "must show help link" in {
        val mainContent = doc.getElementById("main-content")

        val helpLink = mainContent.getElementsByClass("govuk-link hmrc-report-technical-issue ")
        helpLink.size() mustBe 1
      }
    }
  }

}
