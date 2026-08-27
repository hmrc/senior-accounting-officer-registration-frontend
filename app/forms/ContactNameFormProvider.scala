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

package forms

import forms.mappings.Mappings
import models.ContactType
import models.ContactType.First
import play.api.data.Form

import scala.util.matching.Regex

import javax.inject.Inject

class ContactNameFormProvider @Inject() extends Mappings {

  private val legacyIllegalCharsRegex: Regex      = """[<>"&]""".r
  private val firstContactIllegalCharsRegex: Regex = """[<>"]""".r

  def apply(contactType: ContactType): Form[String] =
    contactType match {
      case First =>
        Form(
          "value" -> text("contactName.error.required.first")
            .verifying(maxLength(105, "contactName.error.length.first"))
            .verifying(
              "contactName.error.invalidChars.first",
              name => firstContactIllegalCharsRegex.findFirstIn(name).isEmpty
            )
        )
      case _ =>
        Form(
          "value" -> text("contactName.error.required")
            .verifying(maxLength(50, "contactName.error.length"))
            .verifying(
              "contactName.error.invalidChars",
              name => legacyIllegalCharsRegex.findFirstIn(name).isEmpty
            )
        )
    }
}
