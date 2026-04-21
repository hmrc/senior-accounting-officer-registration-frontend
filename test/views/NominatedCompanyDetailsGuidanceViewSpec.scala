/*
 * Copyright 2026 HM Revenue & Customs
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
import play.api.i18n.Messages
import views.html.NominatedCompanyDetailsGuidanceView
import views.NominatedCompanyDetailsGuidanceViewSpec.*

class NominatedCompanyDetailsGuidanceViewSpec extends ViewSpecBase[NominatedCompanyDetailsGuidanceView] {

  private def generateView(): Document = Jsoup.parse(SUT().toString)

  "NominatedCompanyDetailsGuidanceView" - {
    val doc: Document = generateView()

    doc.createTestsWithStandardPageElements(
      pageTitle = pageTitle,
      pageHeading = pageHeading,
      showBackLink = true,
      showIsThisPageNotWorkingProperlyLink = true,
      hasError = false
    )

    doc.createTestsWithLargeCaption(
      caption = caption
    )

    doc.createTestsWithParagraphs(paragraphs = paragraphTexts)

    doc.createTestForInsetText(pageInsetText)

    doc.createTestsWithOrWithoutError(hasError = false)

    doc.createTestsWithSubmissionButton(
      action = controllers.routes.NominatedCompanyDetailsGuidanceController.continue(),
      buttonText = submitButtonText)
  }
}

object NominatedCompanyDetailsGuidanceViewSpec {
  val pageHeading = "Provide your nominated company details"
  val pageTitle = "Company Details"
  val caption: String = "Nominated company details"
  val paragraphTexts: List[String] = List(
    "You’ll need to enter the nominated company’s details so we can confirm your company and link it to the correct HMRC records.",
    "You’ll be asked to provide your:",
    "These details must match Companies House records. If they do not, you will not be able to register for the service."
  )

  val bulletPointTexts: List[String] = List(
    "Company Registration Number (CRN)",
    "Unique Taxpayer Reference (UTR)"
  )

  val pageInsetText: String = "If your organisation is not registered with Companies House, nominate a company in your group with a CRN to register. If no company in your group has a CRN, contact HMRC using your usual compliance contact or existing support channels."

  val submitButtonText: String = "Continue"
}
