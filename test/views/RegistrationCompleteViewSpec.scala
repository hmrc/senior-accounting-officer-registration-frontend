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
import config.FrontendAppConfig

class RegistrationCompleteViewSpec extends ViewSpecBase[RegistrationCompleteView] {

  private val testDateTime = ZonedDateTime.of(LocalDateTime.of(2025, 1, 17, 11, 45), ZoneOffset.UTC)

  private val registrationCompleteDetails = RegistrationCompleteDetails(
    companyName = "Test Corp Ltd",
    registrationId = "REG12345",
    registrationDateTime = testDateTime
  )

  "RegistrationCompleteView" - {
    "must generate a view" - {
      FrontendAppConfig.setValue("senior-accounting-officer-hub-frontend.host", "hub-url")
      val doc = Jsoup.parse(SUT(registrationCompleteDetails).toString)

      doc.mustHaveCorrectPageTitle(pageTitle)

      doc.createTestForBackLink(show = false)

      "with a confirmation panel that" - {
        "must have the correct title" - {
          doc.getConfirmationPanel.getPanelTitle.createTestMustShowText(expectedText = panelTitle)
        }

        "must have the correct body" - {
          doc.getConfirmationPanel.getPanelBody.createTestMustShowText(expectedText = panelBody)
        }
      }

      doc.createTestMustShowParagraphsWithContent(expectedParagraphs = paragraphsList)

      "The final paragraph" - {
        doc.getMainContent
          .getParagraphs()
          .last
          .createTestMustShowLink(
            expectedText = "submit a notification and certificate.",
            expectedUrl = "hub-url/senior-accounting-officer"
          )
      }

      doc.createTestMustShowBulletPointsWithContent(expectedTexts = bulletPointTexts)

      "First bullet point" - {
        doc.getMainContent
          .select("li")
          .get(0)
          .createTestMustShowLink(
            expectedText = bulletPointTexts.head,
            expectedUrl = "#"
          )
      }

      "Second bullet point" - {
        doc.getMainContent
          .select("li")
          .get(1)
          .createTestMustShowLink(
            expectedText = bulletPointTexts.last,
            expectedUrl = "#"
          )
      }

      doc.createTestMustShowIsThisPageNotWorkingProperlyLink
    }
  }
}

object RegistrationCompleteViewSpec {
  val pageTitle: String = "SAO Registration Confirmation"

  val panelTitle: String = "Registration Complete"

  val panelBody: String = "Your reference ID REG12345"

  val paragraphsList: List[String] = List(
    "Test Corp Ltd has successfully registered to report for Senior Accounting Officer Notification and Certificate service, on 17 January 2025 at 11:45am (GMT).",
    "We have sent a confirmation email with your reference ID to al the contact you gave during registration.",
    "If you need to keep a record of your registration",
    "You can now log into your Senior Accounting Officer notification and certificate service account to submit a notification and certificate."
  )

  val bulletPointTexts: List[String] = List("Print the page", "Download as PDF")

}
