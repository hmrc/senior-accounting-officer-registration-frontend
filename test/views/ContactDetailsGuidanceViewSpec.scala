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
      pageTitle = pageTitle,
      pageHeading = pageHeading,
      showBackLink = true,
      showIsThisPageNotWorkingProperlyLink = true,
      hasError = false
    )

    doc.createTestsWithCaption(
      caption = caption
    )

    doc.createTestsWithParagraphs(paragraphs = paragraphTexts)

    doc.createTestsWithBulletPoints(bullets = bulletPointTexts)

    doc.createTestsWithSubmissionButton(
      action = controllers.routes.ContactDetailsGuidanceController.continue(),
      buttonText = submitButtonText
    )
  }
}

object ContactDetailsGuidanceViewSpec {
  val pageHeading: String = "Provide contact details for your nominated company"

  val caption: String = "Contact details"

  val pageTitle: String = s"$pageHeading - $caption"

  val paragraphTexts: List[String] = List(
    "You need to provide the contact details of the person or team who will submit your company’s SAO notification and certificate.",
    "We’ll use these details to:",
    "You can add up to 2 contacts. For each one, provide their full name and email address."
  )

  val bulletPointTexts: List[String] = List(
    "contact you if we have any questions about your SAO submission",
    "send you confirmation when your registration is complete and when you submit your notification and certificate"
  )

  val submitButtonText: String = "Continue"
}
