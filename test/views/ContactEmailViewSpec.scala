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
import models.{ContactType, NormalMode}
import org.jsoup.Jsoup
import views.ContactEmailViewSpec.*
import views.html.ContactEmailView

class ContactEmailViewSpec extends ViewSpecBase[ContactEmailView] {

  val formProvider: ContactEmailFormProvider = app.injector.instanceOf[ContactEmailFormProvider]

  "ContactEmailView" - {
    ContactType.values.foreach { contactType =>
      s"must generate a view for $contactType contact" - {
        val doc =
          Jsoup.parse(SUT(formProvider().bind(Map("value" -> inputTestValue)), contactType, NormalMode).toString)

        testMustHaveCorrectPageHeading(doc, pageHeading)
        testMustHaveSubmitButton(doc, submitButtonContent)
        testMustShowBackLink(doc)
        testMustShowIsThisPageNotWorkingProperlyLink(doc)
        testMustShowCorrectHintsWithCorrectContent(doc, hintContent)
        testMustShowCorrectInputsWithCorrectDefaultValues(doc, totalInputCount, List(inputTestValue))
        contactType match {
          case First =>
            testMustShowCorrectCaptionsWithCorrectContent(doc, contactType1Caption)
          case Second =>
            testMustShowCorrectCaptionsWithCorrectContent(doc, contactType2Caption)
          case Third =>
            testMustShowCorrectCaptionsWithCorrectContent(doc, contactType3Caption)
        }
      }
    }
  }
}

object ContactEmailViewSpec {
  val pageHeading               = "Enter email address"
  val totalHintsCount           = 1
  val hintContent: List[String] = List(
    "We’ll only use this to contact you about the company’s tax accounting arrangements"
  )
  val contactType1Caption: List[String] = List("First contact details")
  val contactType2Caption: List[String] = List("Second contact details")
  val contactType3Caption: List[String] = List("Third contact details")
  val inputTestValue                    = "test Input Value"
  val totalInputCount                   = 1
  val submitButtonContent               = "Continue"
}
