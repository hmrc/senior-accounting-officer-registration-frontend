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
import config.AppConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.CannotAccessServiceViewSpec.*
import views.html.CannotAccessServiceView

class CannotAccessServiceViewSpec extends ViewSpecBase[CannotAccessServiceView] {

  private lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  private def generateView(): Document = Jsoup.parse(SUT().toString)

  "CannotAccessServiceView" - {
    val doc: Document = generateView()

    doc.createTestsWithStandardPageElements(
      pageTitle = pageTitle,
      pageHeading = pageHeading,
      showBackLink = false,
      showIsThisPageNotWorkingProperlyLink = true,
      hasError = false
    )

    doc.createTestsWithOrWithoutError(hasError = false)

    doc.createTestsWithParagraphs(Seq(paragraph1, paragraph2, findOutMoreLinkText))

    "the sign in link" - {
      doc
        .getParagraphs()
        .toSeq(1)
        .createTestWithLink(
          linkText = signInLinkText,
          destinationUrl = appConfig.organisationSignInUrl
        )
    }

    "the find out more link" - {
      doc
        .getParagraphs()
        .toSeq(2)
        .createTestWithLink(
          linkText = findOutMoreLinkText,
          destinationUrl = appConfig.whoCanUseServiceUrl
        )
    }
  }
}

object CannotAccessServiceViewSpec {
  val pageHeading = "You cannot access this service"
  val pageTitle   = "You cannot access this service"
  val paragraph1  =
    "You’ve signed in with an individual account. Only administrators of an organisation account can register to use this service."
  val signInLinkText      = "sign in to Government Gateway with an organisation account"
  val paragraph2: String  = s"If the group still needs to register, $signInLinkText."
  val findOutMoreLinkText = "Find out more about who can use this service"
}
