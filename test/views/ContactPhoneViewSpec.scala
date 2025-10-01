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
import forms.ContactPhoneFormProvider
import models.ContactType.*
import models.{ContactType, Mode}
import org.jsoup.Jsoup
import views.ContactPhoneViewSpec.*
import views.html.ContactPhoneView

class ContactPhoneViewSpec extends ViewSpecBase[ContactPhoneView] {

  val formProvider: ContactPhoneFormProvider = app.injector.instanceOf[ContactPhoneFormProvider]
  val testValue                              = "test input value"

  "ContactPhoneView" - {
    ContactType.values.foreach { contactType =>
      s"must generate a view for $contactType contact" - {
        Mode.values.foreach { mode =>
          s"must generate a view for $contactType contact in $mode" - {
            "when there are no prior data for the page" - {
              val doc =
                Jsoup.parse(
                  SUT(formProvider(), contactType, mode).toString
                )

              doc.mustHaveCorrectPageTitle(pageHeading)

              doc.createTestForBackLink(show = true)

              doc.createTestMustShowCaptionWithContent(
                expectedCaption = contactType match {
                  case First  => contactTypeFirstCaption
                  case Second => contactTypeSecondCaption
                  case Third  => contactTypeThirdCaption
                }
              )

              doc.createTestMustHaveCorrectPageHeading(pageHeading)

              doc.createTestMustShowASingleInput(
                expectedLabel = pageHeading,
                expectedValue = "",
                expectedHint = Some(expectedHints)
              )

              doc.createTestMustHaveASubmissionButtonWhichSubmitsTo(
                expectedAction = controllers.routes.ContactPhoneController.onSubmit(contactType, mode),
                expectedSubmitButtonText = submitButtonText
              )

              doc.createTestMustShowIsThisPageNotWorkingProperlyLink
            }

            "when there exists prior data for the page" - {
              val doc =
                Jsoup.parse(
                  SUT(formProvider().bind(Map("value" -> "test Input Value")), contactType, mode).toString
                )

              doc.mustHaveCorrectPageTitle(pageHeading)

              doc.createTestForBackLink(show = true)

              doc.createTestMustShowCaptionWithContent(
                expectedCaption = contactType match {
                  case First  => contactTypeFirstCaption
                  case Second => contactTypeSecondCaption
                  case Third  => contactTypeThirdCaption
                }
              )

              doc.createTestMustHaveCorrectPageHeading(pageHeading)

              doc.createTestMustShowASingleInput(
                expectedLabel = pageHeading,
                expectedValue = testInputValue,
                expectedHint = Some(expectedHints)
              )

              doc.createTestMustHaveASubmissionButtonWhichSubmitsTo(
                expectedAction = controllers.routes.ContactPhoneController.onSubmit(contactType, mode),
                expectedSubmitButtonText = submitButtonText
              )

              doc.createTestMustShowIsThisPageNotWorkingProperlyLink
            }
          }
        }
      }
    }
  }
}

object ContactPhoneViewSpec {
  val pageHeading: String = "Phone number"

  val contactTypeFirstCaption: String  = "First contact details"
  val contactTypeSecondCaption: String = "Second contact details"
  val contactTypeThirdCaption: String  = "Third contact details"

  val expectedHints: String =
    "We’ll only use this to contact you about the company’s tax accounting arrangements"
  val testInputValue: String = "test Input Value"

  val submitButtonText: String = "Continue"
}
