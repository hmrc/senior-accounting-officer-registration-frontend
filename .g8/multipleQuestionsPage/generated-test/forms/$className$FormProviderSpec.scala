package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class $className$FormProviderSpec extends StringFieldBehaviours {

  val form = new $className$FormProvider()()

  val field1RequiredKey = "$className;format="decap"$.error.$field1Name$.required"
  val field1LengthKey = "$className;format="decap"$.error.$field1Name$.length"

  val field2RequiredKey = "$className;format="decap"$.error.$field2Name$.required"
  val field2LengthKey = "$className;format="decap"$.error.$field2Name$.length"

  ".$field1Name$" - {

    val fieldName = "$field1Name$"
    val maxLength = $field1MaxLength$

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, field1LengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, field1RequiredKey)
    )
  }

  ".$field2Name$" - {

    val fieldName = "$field2Name$"
    val maxLength = $field2MaxLength$

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, field2LengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, field2RequiredKey)
    )
  }

  "error message keys must map to the expected text" - {
    createTestWithErrorMessageAssertion(
      field1RequiredKey,
      "Enter $field1Name$"
    )

    createTestWithErrorMessageAssertion(
      field1LengthKey,
      "$field1Name$ must be 100 characters or less"
    )

    createTestWithErrorMessageAssertion(
      field2RequiredKey,
      "Enter $field2Name$"
    )

    createTestWithErrorMessageAssertion(
      field2LengthKey,
      "$field2Name$ must be 100 characters or less"
    )
  }
}
