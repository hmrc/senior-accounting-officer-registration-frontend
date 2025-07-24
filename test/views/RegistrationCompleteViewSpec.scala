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
import models.registration.RegistrationCompleteDetails
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.{ContactDetailsGuidanceView, RegistrationCompleteView}

import java.time.LocalDateTime
import scala.jdk.CollectionConverters.*

class RegistrationCompleteViewSpec extends SpecBase with GuiceOneAppPerSuite {

  val SUT: RegistrationCompleteView = app.injector.instanceOf[RegistrationCompleteView]
  given request: Request[_]         = FakeRequest()
  given Messages                    = app.injector.instanceOf[MessagesApi].preferred(request)
  private val testDateTime          = LocalDateTime.of(2025, 1, 17, 11, 45, 0)

  private val registrationCompleteDetails = RegistrationCompleteDetails(
    companyName = "Test Corp Ltd",
    registrationId = "REG12345",
    registrationDateTime = testDateTime
  )

  "RegistrationCompleteView" - {
    "must generate a view" - {
      val doc = Jsoup.parse(SUT(registrationCompleteDetails).toString)

      "with the correct heading" in {
        val mainContent = doc.getElementById("main-content")
        val h1          = mainContent.getElementsByTag("h1")
        h1.size() mustBe 1
        h1.get(0).text() mustBe "Registration Complete"
      }

      "with the correct heading panel text" in {
        val mainContent = doc.getElementById("main-content")
        val panel       = mainContent.getElementsByClass("govuk-panel__body")
        panel.get(0).text() mustBe "Your reference number REG12345"
      }

      "with the correct links and texts" in {
        val bullets = doc
          .getElementById("main-content")
          .getElementsByClass("govuk-list govuk-list--bullet")
          .get(0)
          .getElementsByTag("li")
        bullets.get(0).getElementsByClass("govuk-link").text() mustBe "Print the page"
        bullets.get(1).getElementsByClass("govuk-link").text() mustBe "Download as PDF"

        val link3 = doc.getElementById("main-content").getElementsByClass("govuk-body").get(3).getElementsByTag("a")
        link3.text() mustBe "submit a notification and certificate."
      }

      "with the correct paragraphs" in {

        val mainContent = doc.getElementById("main-content")

        val paragraphs = mainContent.getElementsByTag("p")
        paragraphs.size() mustBe 5
        List.from(paragraphs.iterator().asScala).foreach(p => p.attr("class") mustBe "govuk-body")

        paragraphs
          .get(0)
          .text() mustBe
          "Test Corp Ltd has successfully registered to report for Senior Accounting Officer Notification and Certificate service, on 17 January 2025 at 11:45am (GMT)"

        paragraphs
          .get(1)
          .text() mustBe "We have sent a confirmation email with your reference ID to al the contact you gave during registration."
        paragraphs
          .get(2)
          .text() mustBe "If you need to keep a record of your registration"
        paragraphs
          .get(3)
          .text() must include(
          "You can now log into your Senior Accounting Officer notification and certificate service account to"
        )
        paragraphs
          .get(4)
          .text() mustBe "Is this page not working properly? (opens in new tab)"

      }

      "with the correct bullet points" in {
        val mainContent = doc.getElementById("main-content")

        val ul = mainContent.getElementsByTag("ul")
        ul.size() mustBe 1
        ul.attr("class") mustBe "govuk-list govuk-list--bullet"

        val li = ul.get(0).getElementsByTag("li")
        li.size() mustBe 2

        li.get(0)
          .text() mustBe "Print the page"
        li.get(1).text() mustBe "Download as PDF"
      }

      "must have a continue button" in {
        val mainContent = doc.getElementById("main-content")

        mainContent.getElementById("submit").text() mustBe "Continue"
      }

      "must show a back link" in {
        val backLink = doc.getElementsByClass("govuk-back-link")
        backLink.size() mustBe 1
      }

      "must show help link" in {
        val mainContent = doc.getElementById("main-content")

        val helpLink = mainContent.getElementsByClass("govuk-link hmrc-report-technical-issue ")
        helpLink.size() mustBe 1
      }
    }
  }
}
