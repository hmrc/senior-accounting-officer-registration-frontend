package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class NominatedCompanyDetailsGuidanceFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("nominatedCompanyDetailsGuidance.error.required")
        .verifying(maxLength(100, "nominatedCompanyDetailsGuidance.error.length"))
    )
}
