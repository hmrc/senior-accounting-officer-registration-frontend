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

package viewmodels.checkAnswers

import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}
import viewmodels.converters.*
import viewmodels.govuk.summarylist.*

private[checkAnswers] object ContactSummaryRow {

  def row(
      labelMessageKey: String,
      value: String,
      changeUrl: String,
      hiddenTextMessageKey: String,
      testIdPrefix: String
  )(using messages: Messages): SummaryListRow =
    SummaryListRowViewModel(
      key = Key(
        HtmlContent(s"""<span data-test-id="$testIdPrefix-key">${messages(labelMessageKey)}</span>""")
      ),
      value = ValueViewModel(
        HtmlContent(s"""<span data-test-id="$testIdPrefix-value">${HtmlFormat.escape(value)}</span>""")
      ),
      actions = Seq(
        ActionItemViewModel(
          messages("site.change").toText,
          changeUrl
        )
          .withVisuallyHiddenText(messages(hiddenTextMessageKey))
          .withAttribute("data-test-id", s"$testIdPrefix-change-link")
      )
    )
}
