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
import views.UnauthorisedViewSpec.{pageHeading, pageTitle, paragraphs}
import views.html.UnauthorisedView

class UnauthorisedViewSpec extends ViewSpecBase[UnauthorisedView] {
  "UnauthorisedView" - {
    val doc = Jsoup.parse(SUT().toString)

    doc.createTestsWithStandardPageElements(
      pageTitle = pageTitle,
      pageHeading = pageHeading,
      showBackLink = true,
      showIsThisPageNotWorkingProperlyLink = true,
      hasError = false
    )

    doc.createTestsWithParagraphs(paragraphs)
  }
}

object UnauthorisedViewSpec {
  val pageTitle                = "You can’t access this service with this account"
  val pageHeading              = "You can’t access this service with this account"
  val paragraphs: List[String] = List("unauthorised.guidance")
}
