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

sealed trait ContactHaveYouAddedAll

object ContactHaveYouAddedAll extends Enumerable.Implicits {

  case object Yes extends WithName("yes") with ContactHaveYouAddedAll
  case object No  extends WithName("no") with ContactHaveYouAddedAll

  val values: Seq[ContactHaveYouAddedAll] = Seq(
    Yes,
    No
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map { case (value, index) =>
    RadioItem(
      content = Text(messages(s"contactHaveYouAddedAll.${value.toString}")),
      value = Some(value.toString),
      id = Some(s"value_$index")
    )
  }

  implicit val enumerable: Enumerable[ContactHaveYouAddedAll] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
