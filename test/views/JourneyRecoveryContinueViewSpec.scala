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
import views.JourneyRecoveryContinueViewSpec.*
import views.html.JourneyRecoveryContinueView

class JourneyRecoveryContinueViewSpec extends ViewSpecBase[JourneyRecoveryContinueView] {
  "JourneyRecoveryContinueView" - {
    val doc = Jsoup.parse(SUT("").toString)

    doc.createTestsWithStandardPageElements(
      pageTitle = pageTitle,
      pageHeading = pageHeading,
      showBackLink = true,
      showIsThisPageNotWorkingProperlyLink = true,
      hasError = false
    )

    doc.createTestsWithParagraphs(paragraphs)

    "must have a button" in {
      val button = doc.getMainContent.select("a.govuk-button")
      button.size mustBe 1
      button.text() mustBe buttonText
      button.attr("href") mustBe "#"
    }
  }
}

object JourneyRecoveryContinueViewSpec {
  val pageTitle                = "Sorry, there is a problem with the service"
  val pageHeading              = "Sorry, there is a problem with the service"
  val buttonText               = "Continue"
  val paragraphs: List[String] = List("[Add content to explain how to proceed.]", buttonText)
}
