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
import models.{ContactType, NormalMode}
import org.jsoup.Jsoup
import views.ContactPhoneViewSpec.*
import views.html.ContactPhoneView

class ContactPhoneViewSpec extends ViewSpecBase[ContactPhoneView] {

  val formProvider: ContactPhoneFormProvider = app.injector.instanceOf[ContactPhoneFormProvider]
  val testValue                              = "test input value"

  "ContactPhoneView" - {
    ContactType.values.foreach { contactType =>
      s"must generate a view for $contactType contact" - {

        val doc =
          Jsoup.parse(SUT(formProvider().bind(Map("value" -> "test Input Value")), contactType, NormalMode).toString)
        createTestMustHaveCorrectPageHeading(doc, pageHeading)
        createTestMustShowHints(doc, expectedHints = hintContent)
        createTestMustShowInputsWithValues(doc, expectedValues = inputTestValue)
        createTestMustHaveSubmitButton(doc, submitButtonContent)
        createTestMustShowBackLink(doc)
        createTestMustShowIsThisPageNotWorkingProperlyLink(doc)
        createTestMustShowCaptionsWithContent(
          doc,
          expectedCaptions = contactType match {
            case First  => contactTypeFirstCaption
            case Second => contactTypeSecondCaption
            case Third  => contactTypeThirdCaption
          }
        )

      }
    }
  }
}

object ContactPhoneViewSpec {
  val pageHeading                            = "Phone number"
  val inputTestValue: List[String]           = List("test Input Value")
  val contactTypeFirstCaption: List[String]  = List("First contact details")
  val contactTypeSecondCaption: List[String] = List("Second contact details")
  val contactTypeThirdCaption: List[String]  = List("Third contact details")
  val hintContent: List[String]              = List(
    "We’ll only use this to contact you about the company’s tax accounting arrangements"
  )
  val submitButtonContent = "Continue"
}
