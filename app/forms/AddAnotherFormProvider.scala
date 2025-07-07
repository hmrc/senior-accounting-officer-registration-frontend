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
import play.api.data.Form
import play.api.data.Forms.{text => textMapping, optional}

import javax.inject.Inject

class AddAnotherFormProvider @Inject() extends Mappings {

  def apply(): Form[Boolean] =
    Form(
      "addAnother" -> optional(textMapping).transform(
        s => s.exists(_.nonEmpty),
        _ =>
          throw NotImplementedError(
            "the value to addAnother should be hard coded in the view and not read from the form"
          )
      )
    )
}
