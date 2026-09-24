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

package models

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

enum AddAnotherContact(override val toString: String) {
  case Yes extends AddAnotherContact("yes")
  case No  extends AddAnotherContact("no")
}

object AddAnotherContact extends Enumerable.Implicits[AddAnotherContact] {

  override def members: Array[AddAnotherContact] = AddAnotherContact.values

  def options(using messages: Messages): Seq[RadioItem] = members.map { value =>
    RadioItem(
      content = Text(messages(s"addAnotherContact.${value.toString}")),
      value = Some(value.toString),
      id = Some(s"value_${value.ordinal}")
    )
  }

}
