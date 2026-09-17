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
import views.NominatedCompanyDetailsGuidanceViewSpec.*
import views.html.NominatedCompanyDetailsGuidanceView

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
      caption = pageCaption
    )

    doc.createTestsWithParagraphs(paragraphs = paragraphTexts)

    doc.createTestsWithOrWithoutError(hasError = false)

    doc.createTestsWithSubmissionButton(
      action = controllers.routes.NominatedCompanyDetailsGuidanceController.onSubmit(),
      buttonText = submitButtonText
    )

    doc.createTestsWithBulletPoints(bulletPointTexts)
  }
}

object NominatedCompanyDetailsGuidanceViewSpec {
  val pageHeading                  = "What you need to start your registration"
  val pageCaption: String          = "Nominated company details"
  val pageTitle: String            = s"$pageHeading - $pageCaption"
  val paragraphTexts: List[String] = List(
    "You must have the following details ready to continue. We’ll use them to confirm your nominated company.",
    "You’ll need to provide:",
    "The details you enter must match Companies House records. If they do not match, you cannot complete your registration.",
    "If your nominated company does not have a CRN, register using another company in your organisation that has one.",
    "If no company in your organisation has a CRN, contact your Customer Compliance Manager if you have one or email wmbc.saomailbox@hmrc.gov.uk for support."
  )

  val bulletPointTexts: List[String] = List(
    "Company Registration Number (CRN)",
    "Unique Taxpayer Reference (UTR)"
  )

  val submitButtonText: String = "Continue"
}
