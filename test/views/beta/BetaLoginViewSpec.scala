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
        label = pageHeading,
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
        label = pageHeading,
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
        label = pageHeading,
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
    }
  }
}

object BetaLoginViewSpec {
  val pageHeading = "Enter Senior Accounting Officer notification and certificate private beta password"
  val pageTitle   = "Enter Senior Accounting Officer notification and certificate private beta password"
}
