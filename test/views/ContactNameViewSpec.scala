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
import forms.ContactNameFormProvider
import models.ContactType.*
import models.{ContactType, Mode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ContactNameViewSpec.*
import views.html.ContactNameView

class ContactNameViewSpec extends ViewSpecBase[ContactNameView] {

  val formProvider: ContactNameFormProvider = app.injector.instanceOf[ContactNameFormProvider]

  "ContactNameView" - {
    ContactType.values.foreach { contactType =>
      s"must generate a view for $contactType contact" - {
        Mode.values.foreach { mode =>
          s"must generate a view for $contactType contact in $mode" - {
            "when there are no prior data for the page" - {
              val doc: Document =
                Jsoup.parse(SUT(formProvider(), contactType, mode).toString)

              doc.createTestsWithStandardPageElements(
                pageTitle = pageHeading,
                pageHeading = pageHeading,
                showBackLink = true,
                showIsThisPageNotWorkingProperlyLink = true
              )

              doc.createTestMustShowCaptionWithContent(
                expectedCaption = contactType match {
                  case First  => contactTypeFirstCaption
                  case Second => contactTypeSecondCaption
                  case Third  => contactTypeThirdCaption
                }
              )

              doc.createTestsWithASingleTextInput(
                label = pageHeading,
                value = "",
                hint = Some(expectedHints)
              )

              doc.createTestMustHaveASubmissionButtonWhichSubmitsTo(
                expectedAction = controllers.routes.ContactNameController.onSubmit(contactType, mode),
                expectedSubmitButtonText = submitButtonText
              )
            }

            "when there exists prior data for the page" - {
              val doc: Document =
                Jsoup.parse(SUT(formProvider().bind(Map("value" -> testInputValue)), contactType, mode).toString)

              doc.createTestsWithStandardPageElements(
                pageTitle = pageHeading,
                pageHeading = pageHeading,
                showBackLink = true,
                showIsThisPageNotWorkingProperlyLink = true
              )

              doc.createTestMustShowCaptionWithContent(
                expectedCaption = contactType match {
                  case First  => contactTypeFirstCaption
                  case Second => contactTypeSecondCaption
                  case Third  => contactTypeThirdCaption
                }
              )

              doc.createTestsWithASingleTextInput(
                label = pageHeading,
                value = testInputValue,
                hint = Some(expectedHints)
              )

              doc.createTestMustHaveASubmissionButtonWhichSubmitsTo(
                expectedAction = controllers.routes.ContactNameController.onSubmit(contactType, mode),
                expectedSubmitButtonText = submitButtonText
              )
            }
          }
        }
      }
    }
  }
}

object ContactNameViewSpec {
  val pageHeading: String = "Enter full name"

  val contactTypeFirstCaption: String  = "First contact details"
  val contactTypeSecondCaption: String = "Second contact details"
  val contactTypeThirdCaption: String  = "Third contact details"

  val testInputValue: String = "test Input Value"
  val expectedHints: String  =
    "Add the full name, role and contact details of the person or team that is able to deal with enquiries about the companys account and management of tax accounting arrangements."

  val submitButtonText: String = "Continue"

}
