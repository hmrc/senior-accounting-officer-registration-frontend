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

import controllers.routes
import models.CheckMode
import models.ContactType
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}
import viewmodels.converters.*
import viewmodels.govuk.summarylist.*

object ContactEmailSummary {

  def row(contactType: ContactType, email: String)(using messages: Messages): SummaryListRow =
    SummaryListRowViewModel(
      key = Key(
        HtmlContent(s"""<span data-test-id="first-contact-email-key">${messages("contactEmail.checkYourAnswersLabel")}</span>""")
      ),
      value = ValueViewModel(HtmlContent(s"""<span data-test-id="first-contact-email-value">${HtmlFormat.escape(email)}</span>""")),
      actions = Seq(
        ActionItemViewModel(
          messages("site.change").toText,
          routes.ContactEmailController.onPageLoad(contactType, CheckMode).url
        )
          .withVisuallyHiddenText(messages(s"contactEmail.change.${contactType.messageKey}.hidden"))
          .withAttribute("data-test-id", "first-contact-email-change-link")
      )
    )
}
