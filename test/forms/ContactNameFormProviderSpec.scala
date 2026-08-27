/*
 * Copyright 2026 HM Revenue & Customs
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

import forms.behaviours.StringFieldBehaviours
import models.ContactType.{First, Second}
import play.api.data.FormError

class ContactNameFormProviderSpec extends StringFieldBehaviours {

  private val firstRequiredKey     = "contactName.error.required.first"
  private val firstLengthKey       = "contactName.error.length.first"
  private val firstInvalidCharsKey = "contactName.error.invalidChars.first"
  private val firstMaxLength       = 105

  private val secondRequiredKey     = "contactName.error.required"
  private val secondLengthKey       = "contactName.error.length"
  private val secondInvalidCharsKey = "contactName.error.invalidChars"
  private val secondMaxLength       = 50

  private val firstForm  = new ContactNameFormProvider()(First)
  private val secondForm = new ContactNameFormProvider()(Second)

  ".value for first contact" - {
    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form = firstForm,
      fieldName = fieldName,
      generator = stringsWithMaxLength(firstMaxLength, exclude = Set('<', '>', '"'))
    )

    behave like fieldWithMaxLength(
      firstForm,
      fieldName,
      maxLength = firstMaxLength,
      lengthError = FormError(fieldName, firstLengthKey, Seq(firstMaxLength))
    )

    behave like mandatoryField(
      firstForm,
      fieldName,
      requiredError = FormError(fieldName, firstRequiredKey)
    )

    "must not bind invalid characters <, > and \"" in {
      List("<", ">", "\"", "test<name", "test>name", """test"name""").foreach { input =>
        val result = firstForm.bind(Map(fieldName -> input))
        result.errors.toList must contain(FormError(fieldName, firstInvalidCharsKey))
      }
    }

    "must allow & for first contact" in {
      val result = firstForm.bind(Map(fieldName -> "Tax & Accounting"))
      result.errors mustBe empty
      result.value mustBe Some("Tax & Accounting")
    }
  }

  ".value for second contact" - {
    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form = secondForm,
      fieldName = fieldName,
      generator = stringsWithMaxLength(secondMaxLength, exclude = specialChars)
    )

    behave like fieldThatBindsInvalidData(
      form = secondForm,
      fieldName = fieldName,
      generator = invalidStringsForNameFieldWithMaxLength(secondMaxLength),
      requiredError = FormError(fieldName, secondInvalidCharsKey)
    )

    behave like fieldWithMaxLength(
      secondForm,
      fieldName,
      maxLength = secondMaxLength,
      lengthError = FormError(fieldName, secondLengthKey, Seq(secondMaxLength))
    )

    behave like mandatoryField(
      secondForm,
      fieldName,
      requiredError = FormError(fieldName, secondRequiredKey)
    )
  }

  "error message keys must map to the expected text" - {
    createTestWithErrorMessageAssertion(
      key = firstRequiredKey,
      message = "Enter the name of the person or team we can contact"
    )

    createTestWithErrorMessageAssertion(
      key = firstLengthKey,
      message = "Name of the person or team must be 105 characters or less"
    )

    createTestWithErrorMessageAssertion(
      key = firstInvalidCharsKey,
      message = "Name of the person or team must not include <, >, or \""
    )

    createTestWithErrorMessageAssertion(
      key = secondRequiredKey,
      message =
        """Enter the name of the person or team who can deal with enquiries about the company’s tax accounting arrangements."""
    )

    createTestWithErrorMessageAssertion(
      key = secondLengthKey,
      message = "Name of the person or team must be 50 characters or less"
    )

    createTestWithErrorMessageAssertion(
      key = secondInvalidCharsKey,
      message = "The name you enter must not include the following characters <, >, \" or &"
    )
  }
}
