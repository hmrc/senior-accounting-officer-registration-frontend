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

import forms.ContactEmailFormProvider.emailRegex
import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class ContactEmailFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("contactEmail.error.required")
        .verifying(maxLength(254, "contactEmail.error.length"))
        .verifying(regexp(emailRegex, "contactEmail.error.format"))
    )

}

object ContactEmailFormProvider {
  // to align with email service https://github.com/hmrc/email/blob/main/app/uk/gov/hmrc/email/emailaddress/EmailAddress.scala#L28C1-L36C6
  val emailRegex = """^([a-zA-Z0-9.!#$%&’'*+/=?^_`{|}~-]+)@([a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*)$"""
}
