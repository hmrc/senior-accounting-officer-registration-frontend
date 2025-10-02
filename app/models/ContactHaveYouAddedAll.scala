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

package models

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

enum ContactHaveYouAddedAll(override val toString: String) {
  case Yes extends ContactHaveYouAddedAll("yes")
  case No  extends ContactHaveYouAddedAll("no")
}

object ContactHaveYouAddedAll extends Enumerable.Implicits[ContactHaveYouAddedAll] {

  override def members: Array[ContactHaveYouAddedAll] = ContactHaveYouAddedAll.values

  def options(using messages: Messages): Seq[RadioItem] = members.map { value =>
    RadioItem(
      content = Text(messages(s"contactHaveYouAddedAll.${value.toString}")),
      value = Some(value.toString),
      id = Some(s"value_${value.ordinal}")
    )
  }

}
