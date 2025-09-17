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
import models.{ContactType, NormalMode}
import org.jsoup.Jsoup
import views.ContactRoleViewSpec.*
import views.html.ContactRoleView

class ContactRoleViewSpec extends ViewSpecBase[ContactRoleView] {

  val formProvider: ContactRoleFormProvider = app.injector.instanceOf[ContactRoleFormProvider]

  "ContactRoleView" - {
    ContactType.values.foreach { contactType =>
      s"must generate a view for $contactType contact" - {
        val doc =
          Jsoup.parse(SUT(formProvider().bind(Map("value" -> inputTestValue)), contactType, NormalMode).toString)
        mustHaveCorrectPageHeading(doc, pageHeading)
        mustShowCorrectHintsWithCorrectContent(doc, hintContent.size, hintContent)
        mustShowCorrectInputsWithCorrectDefaultValues(doc, totalInputCount, List(inputTestValue))
        contactType match {
          case First =>
            mustShowCorrectCaptionsWithCorrectContent(doc, captionCountPerPage, contactType1Caption)
          case Second =>
            mustShowCorrectCaptionsWithCorrectContent(doc, captionCountPerPage, contactType2Caption)
          case Third =>
            mustShowCorrectCaptionsWithCorrectContent(doc, captionCountPerPage, contactType3Caption)
        }
        mustShowABackLink(doc)
        mustShowIsThisPageNotWorkingProperlyLink(doc)
        mustHaveSubmitButton(doc, submitButtonContent)
      }
    }
  }
}

object ContactRoleViewSpec {
  val pageHeading                       = "Enter role"
  val inputTestValue                    = "test input value"
  val totalInputCount                   = 1
  val contactType1Caption: List[String] = List("First contact details")
  val contactType2Caption: List[String] = List("Second contact details")
  val contactType3Caption: List[String] = List("Third contact details")
  val captionCountPerPage               = 1
  val hintContent: List[String]         = List("For example, ‘Chief Financial Officer’.")
  val submitButtonContent               = "Continue"

}
