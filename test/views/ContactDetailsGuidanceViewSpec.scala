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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ContactDetailsGuidanceViewSpec.*
import views.html.ContactDetailsGuidanceView

class ContactDetailsGuidanceViewSpec extends ViewSpecBase[ContactDetailsGuidanceView] {

  "ContactDetailsGuidanceView" - {
    val doc: Document = Jsoup.parse(SUT().toString)

    doc.createTestsWithStandardPageElements(
      pageTitle = caption,
      pageHeading = pageHeading,
      showBackLink = true,
      showIsThisPageNotWorkingProperlyLink = true
    )

    doc.createTestMustShowCaptionWithContent(
      expectedCaption = caption
    )

    doc.createTestMustShowParagraphsWithContent(expectedParagraphs = paragraphTexts)

    doc.createTestMustShowBulletPointsWithContent(expectedTexts = bulletPointTexts)

    doc.createTestMustHaveASubmissionButtonWhichSubmitsTo(
      expectedAction = controllers.routes.ContactDetailsGuidanceController.continue(),
      expectedSubmitButtonText = submitButtonText
    )

  }
}

object ContactDetailsGuidanceViewSpec {
  val pageHeading: String = "We need contact details for this [company/group]"

  val caption: String = "Contact details"

  val paragraphTexts: List[String] = List(
    "Provide HMRC with contact details for the person or team responsible for this company or group.",
    "We’ll use these details to:",
    "You could also include the Senior Accounting Officer’s contact details so they can stay informed."
  )

  val bulletPointTexts: List[String] = List(
    "contact the right person if we have questions about the company’s tax accounting arrangements",
    "send confirmation when the notification and certificate have been submitted"
  )

  val submitButtonText: String = "Continue"
}
