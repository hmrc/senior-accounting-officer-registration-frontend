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
import models.{ContactType, NormalMode}
import org.jsoup.Jsoup
import views.ContactNameViewSpec.*
import views.html.ContactNameView

class ContactNameViewSpec extends ViewSpecBase[ContactNameView] {

  val formProvider: ContactNameFormProvider = app.injector.instanceOf[ContactNameFormProvider]

  "ContactNameView" - {
    ContactType.values.foreach { contactType =>
      s"must generate a view for $contactType contact" - {
        val doc =
          Jsoup.parse(SUT(formProvider().bind(Map("value" -> inputTestValue)), contactType, NormalMode).toString)
        testMustHaveCorrectPageHeading(doc, pageHeading)
        testMustShowHintsWithContent(doc, hintContent)
        testMustShowtInputsWithDefaultValues(doc, List(inputTestValue))
        testMustHaveSubmitButton(doc, submitButtonContent)
        testMustShowBackLink(doc)
        testMustShowIsThisPageNotWorkingProperlyLink(doc)
        contactType match {
          case First =>
            testMustShowCaptionsWithContent(doc, contactType1Caption)
          case Second =>
            testMustShowCaptionsWithContent(doc, contactType2Caption)
          case Third =>
            testMustShowCaptionsWithContent(doc, contactType3Caption)
        }
      }
    }
  }
}

object ContactNameViewSpec {
  val pageHeading                       = "Enter full name"
  val inputTestValue                    = "test Input Value"
  val contactType1Caption: List[String] = List("First contact details")
  val contactType2Caption: List[String] = List("Second contact details")
  val contactType3Caption: List[String] = List("Third contact details")
  val submitButtonContent               = "Continue"
  val hintContent: List[String]         = List(
    "Add the full name, role and contact details of the person or team that is able to deal with enquiries about the companys account and management of tax accounting arrangements."
  )
}
