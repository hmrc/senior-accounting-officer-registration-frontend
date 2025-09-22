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
      createTestMustHaveCorrectPageHeading(doc, pageHeading)
      createTestMustShowPanelHeadingsWithContent(doc, expectedPanelHeadings = panelHeadingContent)
      createTestMustShowBulletPointsWithContent(doc, expectedContentList = bulletsContentList)
      createTestMustShowParagraphsWithContent(doc, expectedParagraphs = paragrapsList)
      createTestMustShowBackLink(doc)
      createTestMustShowIsThisPageNotWorkingProperlyLink(doc)
      "First bullet point" - {
        createTestMustShowLink(
          doc.getMainContent.select("li").get(0),
          expectedContent = bulletsContentList.head,
          expectedUrl = "#"
        )
      }
      "Second bullet point" - {
        createTestMustShowLink(
          doc.getMainContent.select("li").get(1),
          expectedContent = bulletsContentList.last,
          expectedUrl = "#"
        )
      }
      "The final paragraph" - {
        createTestMustShowLink(
          doc.getMainContent.select("p").get(3),
          expectedContent = "submit a notification and certificate.",
          expectedUrl = "/beta/beta-sao-digitalisation-dashboard.html"
        )
      }
    }
  }
}

object RegistrationCompleteViewSpec {
  val paragrapsList: List[String] = List(
    "Test Corp Ltd has successfully registered to report for Senior Accounting Officer Notification and Certificate service, on 17 January 2025 at 11:45am (GMT).",
    "We have sent a confirmation email with your reference ID to al the contact you gave during registration.",
    "If you need to keep a record of your registration",
    "You can now log into your Senior Accounting Officer notification and certificate service account to submit a notification and certificate."
  )
  val paragraphSubstring =
    "You can now log into your Senior Accounting Officer notification and certificate service account to "

  val expectedLinkContentWithUrls: List[(String, String)] = List(
    ("Print the page", "#"),
    ("Download as PDF", "#"),
    ("submit a notification and certificate.", "/beta/beta-sao-digitalisation-dashboard.html")
  )
  val bulletsContentList: List[String]  = List("Print the page", "Download as PDF")
  val panelHeadingContent: List[String] = List("Your reference number REG12345")
  val pageHeading                       = "Registration Complete"
}
