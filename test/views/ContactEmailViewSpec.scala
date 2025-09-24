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
import forms.ContactEmailFormProvider
import models.ContactType.*
import models.{ContactType, Mode}
import org.jsoup.Jsoup
import views.ContactEmailViewSpec.*
import views.html.ContactEmailView

class ContactEmailViewSpec extends ViewSpecBase[ContactEmailView] {

  val formProvider: ContactEmailFormProvider = app.injector.instanceOf[ContactEmailFormProvider]

  "ContactEmailView" - {
    ContactType.values.foreach { contactType =>
      s"must generate a view for $contactType contact" - {
        Mode.values.foreach { mode =>
          s"must generate a view for $contactType contact in $mode" - {
            "when there are no prior data for the page" - {
              val doc =
                Jsoup.parse(SUT(formProvider(), contactType, mode).toString)

              createTestMustShowBackLink(doc)

              createTestMustShowCaptionsWithContent(
                doc,
                expectedCaptions = contactType match {
                  case First  => contactTypeFirstCaption
                  case Second => contactTypeSecondCaption
                  case Third  => contactTypeThirdCaption
                }
              )

              createTestMustHaveCorrectPageHeading(doc, pageHeading)

              doc.createTestMustShowASingleInput(
                expectedLabel = pageHeading,
                expectedValue = "",
                expectedHint = Some(expectedHints)
              )

              doc.createTestMustHaveASubmissionButtonWhichSubmitsTo(
                expectedAction = controllers.routes.ContactEmailController.onSubmit(contactType, mode),
                expectedSubmitButtonText = submitButtonText
              )

              createTestMustShowIsThisPageNotWorkingProperlyLink(doc)
            }

            "when there exists prior data for the page" - {
              val doc =
                Jsoup.parse(SUT(formProvider().bind(Map("value" -> testInputValue)), contactType, mode).toString)

              createTestMustShowBackLink(doc)

              createTestMustShowCaptionsWithContent(
                doc,
                expectedCaptions = contactType match {
                  case First  => contactTypeFirstCaption
                  case Second => contactTypeSecondCaption
                  case Third  => contactTypeThirdCaption
                }
              )

              createTestMustHaveCorrectPageHeading(doc, pageHeading)

              doc.createTestMustShowASingleInput(
                expectedLabel = pageHeading,
                expectedValue = testInputValue,
                expectedHint = Some(expectedHints)
              )

              doc.createTestMustHaveASubmissionButtonWhichSubmitsTo(
                expectedAction = controllers.routes.ContactEmailController.onSubmit(contactType, mode),
                expectedSubmitButtonText = submitButtonText
              )

              createTestMustShowIsThisPageNotWorkingProperlyLink(doc)
            }
          }
        }
      }
    }
  }
}

object ContactEmailViewSpec {
  val pageHeading = "Enter email address"

  val contactTypeFirstCaption: List[String]  = List("First contact details")
  val contactTypeSecondCaption: List[String] = List("Second contact details")
  val contactTypeThirdCaption: List[String]  = List("Third contact details")

  val expectedHints: String = "We’ll only use this to contact you about the company’s tax accounting arrangements"
  val testInputValue        = "test Input Value"

  val submitButtonText = "Continue"
}
