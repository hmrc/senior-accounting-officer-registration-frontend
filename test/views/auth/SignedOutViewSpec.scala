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

package views.auth

import base.ViewSpecBase
import controllers.routes
import org.jsoup.Jsoup
import views.auth.SignedOutViewSpec.{buttonText, pageHeading, paragraphs}
import views.html.auth.SignedOutView

class SignedOutViewSpec extends ViewSpecBase[SignedOutView] {
  "SignedOutView" - {
    val doc = Jsoup.parse(SUT().toString)

    doc.createTestsWithStandardPageElements(
      pageTitle = pageHeading,
      pageHeading = pageHeading,
      showBackLink = false,
      showIsThisPageNotWorkingProperlyLink = true,
      hasError = false
    )

    doc.createTestsWithParagraphs(paragraphs)

    "must have a button" in {
      val button = doc.getMainContent.select("a.govuk-button")
      button.size mustBe 1
      button.text() mustBe buttonText
      button.attr("href") mustBe routes.IndexController.onPageLoad().url
    }
  }
}

object SignedOutViewSpec {
  val pageHeading: String      = "For your security, we signed you out"
  val buttonText               = "Sign in"
  val paragraphs: List[String] = List("We did not save your answers.", buttonText)
}
