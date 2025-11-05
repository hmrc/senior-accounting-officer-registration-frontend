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
import org.jsoup.nodes.Document
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
              val doc: Document =
                Jsoup.parse(SUT(formProvider(), contactType, mode).toString)

              doc.createTestsWithStandardPageElements(
                pageTitle = contactType match {
                  case First  => pageTitleFirst
                  case Second => pageTitleSecond
                },
                pageHeading = pageHeading,
                showBackLink = true,
                showIsThisPageNotWorkingProperlyLink = true,
                hasError = false
              )

              doc.createTestsWithCaption(
                caption = contactType match {
                  case First  => contactTypeFirstCaption
                  case Second => contactTypeSecondCaption
                }
              )

              doc.createTestsWithASingleTextInput(
                name = "value",
                label = pageHeading,
                value = "",
                hint = Some(expectedHints),
                hasError = false
              )

              doc.createTestsWithSubmissionButton(
                action = controllers.routes.ContactEmailController.onSubmit(contactType, mode),
                buttonText = submitButtonText
              )

            }

            "when there exists prior data for the page" - {
              val doc: Document =
                Jsoup.parse(SUT(formProvider().bind(Map("value" -> testInputValue)), contactType, mode).toString)

              doc.createTestsWithStandardPageElements(
                pageTitle = contactType match {
                  case First  => pageTitleFirst
                  case Second => pageTitleSecond
                },
                pageHeading = pageHeading,
                showBackLink = true,
                showIsThisPageNotWorkingProperlyLink = true,
                hasError = false
              )

              doc.createTestsWithCaption(
                caption = contactType match {
                  case First  => contactTypeFirstCaption
                  case Second => contactTypeSecondCaption
                }
              )

              doc.createTestsWithASingleTextInput(
                name = "value",
                label = pageHeading,
                value = testInputValue,
                hint = Some(expectedHints),
                hasError = false
              )

              doc.createTestsWithSubmissionButton(
                action = controllers.routes.ContactEmailController.onSubmit(contactType, mode),
                buttonText = submitButtonText
              )
            }

            "when the page has an error" - {
              val doc: Document =
                Jsoup.parse(SUT(formProvider().withError("value", "broken"), contactType, mode).toString)

              doc.createTestsWithStandardPageElements(
                pageTitle = contactType match {
                  case First  => pageTitleFirst
                  case Second => pageTitleSecond
                },
                pageHeading = pageHeading,
                showBackLink = true,
                showIsThisPageNotWorkingProperlyLink = true,
                hasError = true
              )

              doc.createTestsWithCaption(
                caption = contactType match {
                  case First  => contactTypeFirstCaption
                  case Second => contactTypeSecondCaption
                }
              )

              doc.createTestsWithASingleTextInput(
                name = "value",
                label = pageHeading,
                value = "",
                hint = Some(expectedHints),
                hasError = true
              )

              doc.createTestsWithSubmissionButton(
                action = controllers.routes.ContactEmailController.onSubmit(contactType, mode),
                buttonText = submitButtonText
              )
            }
          }
        }
      }
    }
  }
}

object ContactEmailViewSpec {
  val pageHeading: String = "Enter email address"

  val contactTypeFirstCaption: String  = "First contact details"
  val contactTypeSecondCaption: String = "Second contact details"

  val pageTitleFirst: String  = "First contact details"
  val pageTitleSecond: String = "Second contact details"

  val expectedHints: String  = "We’ll only use this to contact them about the company’s submission."
  val testInputValue: String = "test Input Value"

  val submitButtonText: String = "Continue"
}
