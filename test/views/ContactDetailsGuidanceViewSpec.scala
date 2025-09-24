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
import views.ContactDetailsGuidanceViewSpec.*
import views.html.ContactDetailsGuidanceView

class ContactDetailsGuidanceViewSpec extends ViewSpecBase[ContactDetailsGuidanceView] {

  "ContactDetailsGuidanceView" - {
    val doc = Jsoup.parse(SUT().toString)

    createTestMustShowBackLink(doc)

    createTestMustHaveCorrectPageHeading(doc, expectedHeading = pageHeading)

    createTestMustShowParagraphsWithContent(doc, expectedParagraphs = paragraphsContent)

    createTestMustShowBulletPointsWithContent(doc, expectedContentList = bulletPointsContent)

    doc.createTestMustHaveASubmissionButtonWhichSubmitsTo(
      expectedAction = controllers.routes.ContactDetailsGuidanceController.continue(),
      expectedSubmitButtonText = submitButtonText
    )

    createTestMustShowIsThisPageNotWorkingProperlyLink(doc)
  }
}

object ContactDetailsGuidanceViewSpec {
  val pageHeading = "We need contact details for this [company/group]"

  val paragraphsContent: List[String] = List(
    "Provide HMRC with contact details for the person or team responsible for this company or group.",
    "We’ll use these details to:",
    "You could also include the Senior Accounting Officer’s contact details so they can stay informed."
  )

  val bulletPointsContent: List[String] = List(
    "contact the right person if we have questions about the company’s tax accounting arrangements",
    "send confirmation when the notification and certificate have been submitted"
  )

  val submitButtonText = "Continue"
}
