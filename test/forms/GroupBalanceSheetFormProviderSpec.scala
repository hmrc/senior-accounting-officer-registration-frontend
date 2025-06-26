package forms

import forms.behaviours.OptionFieldBehaviours
import models.GroupBalanceSheet
import play.api.data.FormError

class GroupBalanceSheetFormProviderSpec extends OptionFieldBehaviours {

  val form = new GroupBalanceSheetFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "groupBalanceSheet.error.required"

    behave like optionsField[GroupBalanceSheet](
      form,
      fieldName,
      validValues  = GroupBalanceSheet.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
