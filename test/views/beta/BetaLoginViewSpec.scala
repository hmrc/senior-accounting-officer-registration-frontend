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

package views.beta

import base.ViewSpecBase
import config.AppConfig
import forms.beta.BetaLoginFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.beta.BetaLoginViewSpec.*
import views.html.beta.BetaLoginView

class BetaLoginViewSpec extends ViewSpecBase[BetaLoginView] {

  private val formProvider       = app.injector.instanceOf[BetaLoginFormProvider]
  private val testPassword       = app.injector.instanceOf[AppConfig].privateBetaPassword
  private val form: Form[String] = formProvider()

  private def generateView(form: Form[String]): Document = {
    val view = SUT(form)
    Jsoup.parse(view.toString)
  }

  "BetaLoginView" - {

    "when the form is not filled in" - {
      val doc = generateView(form)

      doc.createTestsWithStandardPageElements(
        pageTitle = pageTitle,
        pageHeading = pageHeading,
        showBackLink = false,
        showIsThisPageNotWorkingProperlyLink = true,
        hasError = false
      )

      doc.createTestsWithASingleTextInput(
        name = "value",
        label = pageLabel,
        value = "",
        hint = None,
        hasError = false
      )

      doc.createTestsWithSubmissionButton(
        action = controllers.beta.routes.BetaLoginController.onSubmit(),
        buttonText = "Continue"
      )

      doc.createTestsWithOrWithoutError(
        hasError = false
      )

      doc.createTestsWithParagraphs(paragraphs)

      doc.createTestsForSubHeadings(pageSubHeadings)

      doc.createTestsWithBulletPoints(bulletPointTexts)

    }

    "when the form is filled in" - {
      val doc = generateView(form.bind(Map("value" -> testPassword)))

      doc.createTestsWithStandardPageElements(
        pageTitle = pageTitle,
        pageHeading = pageHeading,
        showBackLink = false,
        showIsThisPageNotWorkingProperlyLink = true,
        hasError = false
      )

      doc.createTestsWithASingleTextInput(
        name = "value",
        label = pageLabel,
        value = testPassword,
        hint = None,
        hasError = false
      )

      doc.createTestsWithSubmissionButton(
        action = controllers.beta.routes.BetaLoginController.onSubmit(),
        buttonText = "Continue"
      )

      doc.createTestsWithOrWithoutError(
        hasError = false
      )
      doc.createTestsForSubHeadings(pageSubHeadings)

      doc.createTestsWithBulletPoints(bulletPointTexts)

    }

    "when the form has errors" - {
      val doc = generateView(form.withError("value", "broken"))

      doc.createTestsWithStandardPageElements(
        pageTitle = pageTitle,
        pageHeading = pageHeading,
        showBackLink = false,
        showIsThisPageNotWorkingProperlyLink = true,
        hasError = true
      )

      doc.createTestsWithASingleTextInput(
        name = "value",
        label = pageLabel,
        value = "",
        hint = None,
        hasError = true
      )

      doc.createTestsWithSubmissionButton(
        action = controllers.beta.routes.BetaLoginController.onSubmit(),
        buttonText = "Continue"
      )

      doc.createTestsWithOrWithoutError(
        hasError = true
      )

      doc.createTestsWithParagraphs(paragraphs)

      doc.createTestsForSubHeadings(pageSubHeadingsWithError)

      doc.createTestsWithBulletPoints(bulletPointTexts)

    }
  }

  extension (target: => Document) {
    def createTestsForSubHeadings(subheadings: Seq[String]): Unit = {
      val headings = target.getMainContent.getElementsByTag("h2")
      "must have expected number of headings" in {
        headings.size() mustBe subheadings.length
      }
      subheadings.zipWithIndex.foreach((subheading, i) => {
        s"must have heading '$subheading'" in {
          headings.get(i).text mustBe subheading
        }
      })
    }

  }
}

object BetaLoginViewSpec {
  val pageHeading                           = "Enter your password"
  val pageTitle                             = "Enter your password"
  val pageLabel                             = "You should have received this in your introduction pack."
  val pageSubHeadings: Seq[String]          = Seq("If you have not received a password")
  val pageSubHeadingsWithError: Seq[String] = Seq("There is a problem", "If you have not received a password")
  val paragraphs: Seq[String]               = Seq(
    "The Senior Accounting Officer notification and certificate service is in private beta. You’ll need a password to access it.",
    "If you believe you should have access to this service, you can:"
  )
  val bulletPointTexts: List[String] = List(
    "check your spam or junk folder for an email from email@gov.uk",
    "contact the team at email@example.com"
  )

}
