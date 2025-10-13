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
import models.ContactType
import models.ContactType.*
import org.jsoup.Jsoup
import views.html.ContactHaveYouAddedAllView

class ContactHaveYouAddedAllViewSpec extends ViewSpecBase[ContactHaveYouAddedAllView] {

  val formProvider: ContactHaveYouAddedAllFormProvider = app.injector.instanceOf[ContactHaveYouAddedAllFormProvider]
  val pageTitle: String                                = "First contact details"
  val pageHeading: String                              = "Have you added all the contacts you need?"
  val pageHint: String                                 =
    "Provide more than one contact if possible, in case we do not get a response from the primary contact. You can add up to two contact details."

  "ContactHaveYouAddedAllView" - {
    "when there are no prior data for the page" - {
      val doc = Jsoup.parse(SUT(formProvider(), First).toString)

      doc.mustHaveCorrectPageTitle(pageTitle)
      doc.createTestMustHaveCorrectPageHeading(pageHeading)
      doc.createTestMustShowHint(pageHint)

      doc.createTestsWithRadioButtons(
        values = List("yes", "no"),
        labels = List("Yes", "No, add another contact")
      )

      doc.createTestMustHaveASubmissionButtonWhichSubmitsTo(
        expectedAction = controllers.routes.ContactHaveYouAddedAllController.onSubmit(First),
        expectedSubmitButtonText = "Continue"
      )

      doc.createTestMustShowIsThisPageNotWorkingProperlyLink
    }
  }
}
