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
import config.FrontendAppConfig
import models.registration.RegistrationCompleteDetails
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
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
      FrontendAppConfig.setValue("senior-accounting-officer-hub-frontend.host", "hub-url")

      val doc: Document = Jsoup.parse(SUT(registrationCompleteDetails).toString)

      doc.createTestsWithStandardPageElements(
        pageTitle = pageTitle,
        pageHeading = panelTitle,
        showBackLink = false,
        showIsThisPageNotWorkingProperlyLink = true
      )

      "with a confirmation panel that" - {
        "must have the correct title" - {
          doc.getConfirmationPanel.getPanelTitle.createTestWithText(text = panelTitle)
        }

        "must have the correct body" - {
          doc.getConfirmationPanel.getPanelBody.createTestWithText(text = panelBody)
        }
      }

      doc.createTestsWithParagraphs(paragraphs = paragraphsList)

      "The final paragraph" - {
        doc.getMainContent
          .getParagraphs()
          .last
          .createTestWithLink(
            linkText = "submit a notification and certificate.",
            destinationUrl = "hub-url/senior-accounting-officer"
          )
      }

      doc.createTestsWithBulletPoints(bullets = bulletPointTexts)

      "First bullet point" - {
        doc.getMainContent
          .select("li")
          .get(0)
          .createTestWithLink(
            linkText = bulletPointTexts.head,
            destinationUrl = "#"
          )
      }

      "Second bullet point" - {
        doc.getMainContent
          .select("li")
          .get(1)
          .createTestWithLink(
            linkText = bulletPointTexts.last,
            destinationUrl = "#"
          )
      }

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
