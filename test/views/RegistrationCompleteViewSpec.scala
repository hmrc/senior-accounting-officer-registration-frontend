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

import base.ViewSpecBase
import models.registration.RegistrationCompleteDetails
import org.jsoup.Jsoup
import views.RegistrationCompleteViewSpec.*
import views.html.RegistrationCompleteView

import java.time.{LocalDateTime, ZoneOffset, ZonedDateTime}

class RegistrationCompleteViewSpec extends ViewSpecBase[RegistrationCompleteView] {

  private val testDateTime = ZonedDateTime.of(LocalDateTime.of(2025, 1, 17, 11, 45), ZoneOffset.UTC)

  private val registrationCompleteDetails = RegistrationCompleteDetails(
    companyName = "Test Corp Ltd",
    registrationId = "REG12345",
    registrationDateTime = testDateTime
  )

  "RegistrationCompleteView" - {
    "must generate a view" - {
      val doc = Jsoup.parse(SUT(registrationCompleteDetails).toString)
      testMustHaveCorrectPageHeading(doc, "Registration Complete")

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

      testMustShowCorrectParagraphsWithCorrectContent(
        doc,
        paragrapsList
      )
      testMustShowParagraphsContainingContent(
        doc,
        1,
        "You can now log into your Senior Accounting Officer notification and certificate service account to "
      )
      testMustShowCorrectLinksAndCorrectContent(doc, linksContent)
      testMustShowBackLink(doc)
      testMustShowIsThisPageNotWorkingProperlyLink(doc)
    }
  }
}

object RegistrationCompleteViewSpec {
  val paragrapsList = List(
    "Test Corp Ltd has successfully registered to report for Senior Accounting Officer Notification and Certificate service, on 17 January 2025 at 11:45am (GMT).",
    "We have sent a confirmation email with your reference ID to al the contact you gave during registration.",
    "If you need to keep a record of your registration"
  )
  val linksContent = List("Print the page", "Download as PDF", "submit a notification and certificate.")

}
