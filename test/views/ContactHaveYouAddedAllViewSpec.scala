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
import forms.ContactHaveYouAddedAllFormProvider
import models.ContactType.*
import models.{ContactType, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ContactHaveYouAddedAllViewSpec.*
import views.html.ContactHaveYouAddedAllView

class ContactHaveYouAddedAllViewSpec extends ViewSpecBase[ContactHaveYouAddedAllView] {

  val formProvider: ContactHaveYouAddedAllFormProvider = app.injector.instanceOf[ContactHaveYouAddedAllFormProvider]
  "ContactHaveYouAddedAllView" - {
    "when there are no prior data for the page" - {
      val doc: Document = Jsoup.parse(SUT(formProvider(), First, NormalMode).toString)

      doc.createTestsWithStandardPageElements(
        pageTitle = pageTitle,
        pageHeading = pageHeading,
        showBackLink = true,
        showIsThisPageNotWorkingProperlyLink = true,
        hasError = false
      )

      doc.createTestsWithParagraphs(List(pageHint))

      doc.createTestWithSubheading(subheading = subheading)

      doc.createTestsWithRadioButtons(
        name = "value",
        radios = List(
          radio(value = "yes", label = "Yes"),
          radio(value = "no", label = "No")
        ),
        isChecked = None,
        hasError = false
      )

      doc.createTestsWithSubmissionButton(
        action = controllers.routes.ContactHaveYouAddedAllController.onSubmit(First, NormalMode),
        buttonText = "Continue"
      )
    }

    "when the page is errored" - {
      val doc: Document = Jsoup.parse(SUT(formProvider().withError("value", "broken"), First, NormalMode).toString)

      doc.createTestsWithStandardPageElements(
        pageTitle = pageTitle,
        pageHeading = pageHeading,
        showBackLink = true,
        showIsThisPageNotWorkingProperlyLink = true,
        hasError = true
      )

      doc.createTestWithParagraph(pageHint)

      doc.createTestWithSubheading(subheading = subheading)

      doc.createTestsWithRadioButtons(
        name = "value",
        radios = List(
          radio(value = "yes", label = "Yes"),
          radio(value = "no", label = "No")
        ),
        isChecked = None,
        hasError = true
      )

      doc.createTestsWithSubmissionButton(
        action = controllers.routes.ContactHaveYouAddedAllController.onSubmit(First, NormalMode),
        buttonText = "Continue"
      )
    }
  }
  extension (doc: Document) {
    def createTestWithSubheading(subheading: String): Unit = {
      val subheadings = doc.select("legend > h2")
      "must display the correct subheading" in {
        subheadings.get(0).text() mustBe subheading
        subheadings.size() mustBe 1
      }
    }

    def createTestWithParagraph(pageHint: String): Unit = {
      val paragraphs = doc.select("h1 + p")
      "must display the correct paragraph in the error form" in {
        paragraphs.get(0).text() mustBe pageHint
        paragraphs.size() mustBe 1
      }
    }
  }
}

object ContactHaveYouAddedAllViewSpec {
  val pageHeading: String = "Add another contact"
  val pageCaption: String = "Contact details"
  val pageTitle: String   = s"$pageHeading - $pageCaption"
  val pageHint: String    = {
    "You can add up to 2 contacts. A second contact means we can still email you if we cannot reach the first."
  }
  val subheading: String = "Do you want to add another contact?"
}
