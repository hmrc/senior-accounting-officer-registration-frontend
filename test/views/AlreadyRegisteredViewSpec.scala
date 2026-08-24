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
import views.AlreadyRegisteredViewSpec.*
import views.html.AlreadyRegisteredView

class AlreadyRegisteredViewSpec extends ViewSpecBase[AlreadyRegisteredView] {

  private lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  private def generateView(): Document = Jsoup.parse(SUT().toString)

  "AlreadyRegisteredView" - {
    val doc: Document = generateView()

    doc.createTestsWithStandardPageElements(
      pageTitle = pageTitle,
      pageHeading = pageHeading,
      showBackLink = false,
      showIsThisPageNotWorkingProperlyLink = true,
      hasError = true
    )

    doc.createTestsWithOrWithoutError(hasError = false)

    doc.createTestsWithParagraphs(Seq(paragraph1, paragraph2))

    "the business tax account link" - {
      doc
        .getParagraphs()
        .toSeq
        .head
        .createTestWithLink(
          linkText = businessTaxAccountLinkText,
          destinationUrl = appConfig.businessTaxAccountUrl
        )

      "must open in a new tab" in {
        val link = doc.getMainContent.select("a.govuk-link").first

        link.attr("target") mustBe "_blank"
        link.attr("rel") must include("noopener")
      }
    }
  }
}

object AlreadyRegisteredViewSpec {
  val pageHeading                = "Your company has already registered for this service"
  val pageTitle                  = "Your company has already registered for this service"
  val businessTaxAccountLinkText = "business tax account (opens in new tab)"
  val paragraph1: String         =
    s"You can find a link to the Senior Accounting Officer notification and certificate service on your HMRC $businessTaxAccountLinkText. From there you can manage your account and make a submission."
  val paragraph2 = "Log in using your organisation Government Gateway ID."
}
