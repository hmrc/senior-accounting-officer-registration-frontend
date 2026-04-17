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
import config.AppConfig
import models.registration.RegistrationCompleteDetails
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.RegistrationCompleteViewSpec.*
import views.html.RegistrationCompleteView

class RegistrationCompleteViewSpec extends ViewSpecBase[RegistrationCompleteView] {

  private val registrationCompleteDetails = RegistrationCompleteDetails(
    registrationId = "REG12345"
  )

  "RegistrationCompleteView" - {
    "must generate a view" - {
      AppConfig.setValue("senior-accounting-officer-hub-frontend.host", "hub-url")

      val doc: Document = Jsoup.parse(SUT(registrationCompleteDetails).toString)

      doc.createTestsWithStandardPageElements(
        pageTitle = pageTitle,
        pageHeading = panelTitle,
        showBackLink = false,
        showIsThisPageNotWorkingProperlyLink = true,
        hasError = false
      )

      "with a confirmation panel that" - {
        "must have the correct title" - {
          doc.getConfirmationPanel.getPanelTitle.createTestWithText(panelTitle)
        }

        "must have the correct body" - {
          doc.getConfirmationPanel.getPanelBody.createTestWithText(panelBody)
        }
      }

      doc.createTestsWithParagraphs(paragraphsList)

      doc.getMainContent
        .select("p")
        .get(1)
        .createTestWithLink(
          linkText = "Print this page",
          destinationUrl = "#"
        )

      "Continue button" - {
        doc.getMainContent
          .select(".govuk-button-primary")
          .get(0)
          .createTestWithText("Continue")
      }
    }
  }
}

object RegistrationCompleteViewSpec {
  val pageTitle: String = "SAO Registration Confirmation"

  val panelTitle: String = "Registration Complete"

  val panelBody: String = "Your reference number REG12345"

  val paragraphsList: List[String] = List(
    "We’ve sent a confirmation email to all the contacts you gave during registration.",
    "Print this page if you want to keep a paper record of your registration.",
    "You can now log in to submit a notification and certificate."
  )

}
