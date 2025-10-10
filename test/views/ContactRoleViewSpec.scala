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
import forms.ContactRoleFormProvider
import models.ContactType.*
import models.{ContactType, Mode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.ContactRoleViewSpec.*
import views.html.ContactRoleView

class ContactRoleViewSpec extends ViewSpecBase[ContactRoleView] {

  val formProvider: ContactRoleFormProvider = app.injector.instanceOf[ContactRoleFormProvider]

  "ContactRoleView" - {
    ContactType.values.foreach { contactType =>
      s"must generate a view for $contactType contact" - {
        Mode.values.foreach { mode =>
          s"must generate a view for $contactType contact in $mode" - {
            "when there are no prior data for the page" - {
              val doc: Document =
                Jsoup.parse(
                  SUT(formProvider(), contactType, mode).toString
                )

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

              doc.createTestMustShowASingleInput(
                expectedLabel = pageHeading,
                expectedValue = "",
                expectedHint = Some(expectedHints)
              )

              doc.createTestMustHaveASubmissionButtonWhichSubmitsTo(
                expectedAction = controllers.routes.ContactRoleController.onSubmit(contactType, mode),
                expectedSubmitButtonText = submitButtonText
              )

            }

            "when there exists prior data for the page" - {
              val doc: Document = Jsoup.parse(
                SUT(formProvider().bind(Map("value" -> "test input value")), contactType, mode).toString
              )

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

              doc.createTestMustShowASingleInput(
                expectedLabel = pageHeading,
                expectedValue = testInputValue,
                expectedHint = Some(expectedHints)
              )

              doc.createTestMustHaveASubmissionButtonWhichSubmitsTo(
                expectedAction = controllers.routes.ContactRoleController.onSubmit(contactType, mode),
                expectedSubmitButtonText = submitButtonText
              )

            }
          }
        }
      }
    }
  }
}

object ContactRoleViewSpec {
  val pageHeading: String = "Enter role"

  val contactTypeFirstCaption: String  = "First contact details"
  val contactTypeSecondCaption: String = "Second contact details"
  val contactTypeThirdCaption: String  = "Third contact details"

  val expectedHints: String  = "For example, ‘Chief Financial Officer’."
  val testInputValue: String = "test input value"

  val submitButtonText: String = "Continue"
}
