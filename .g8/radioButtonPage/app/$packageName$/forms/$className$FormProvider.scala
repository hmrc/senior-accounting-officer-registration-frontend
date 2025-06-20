package $packageName$.forms

import javax.inject.Inject

import common.forms.mappings.Mappings
import play.api.data.Form
import $packageName$.models.$className$

class $className$FormProvider @Inject() extends Mappings {

  def apply(): Form[$className$] =
    Form(
      "value" -> enumerable[$className$]("$className;format="decap"$.error.required")
    )
}
