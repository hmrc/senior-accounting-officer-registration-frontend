package forms

import forms.behaviours.OptionFieldBehaviours
import models.IsGroupOrStandalone
import play.api.data.FormError

class IsGroupOrStandaloneFormProviderSpec extends OptionFieldBehaviours {

  val form = new IsGroupOrStandaloneFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "isGroupOrStandalone.error.required"

    behave like optionsField[IsGroupOrStandalone](
      form,
      fieldName,
      validValues  = IsGroupOrStandalone.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
