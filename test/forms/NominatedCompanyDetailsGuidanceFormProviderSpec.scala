package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class NominatedCompanyDetailsGuidanceFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "nominatedCompanyDetailsGuidance.error.required"
  val lengthKey = "nominatedCompanyDetailsGuidance.error.length"
  val maxLength = 100

  val form = new NominatedCompanyDetailsGuidanceFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  "error message keys must map to the expected text" - {
    createTestWithErrorMessageAssertion(
      key = requiredKey,
      message = "Enter nominatedCompanyDetailsGuidance"
    )

    createTestWithErrorMessageAssertion(
      key = lengthKey,
      message = "NominatedCompanyDetailsGuidance must be 100 characters or less"
    )
  }
}
