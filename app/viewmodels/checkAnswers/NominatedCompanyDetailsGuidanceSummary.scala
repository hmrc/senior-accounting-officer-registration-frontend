package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.NominatedCompanyDetailsGuidancePage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.converters.*

object NominatedCompanyDetailsGuidanceSummary  {

  def row(answers: UserAnswers)(using messages: Messages): Option[SummaryListRow] =
    answers.get(NominatedCompanyDetailsGuidancePage).map {
      answer =>

        SummaryListRowViewModel(
          key     = messages("nominatedCompanyDetailsGuidance.checkYourAnswersLabel").toKey,
          value   = ValueViewModel(answer.toText),
          actions = Seq(
            ActionItemViewModel(messages("site.change").toText, routes.NominatedCompanyDetailsGuidanceController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("nominatedCompanyDetailsGuidance.change.hidden"))
          )
        )
    }
}
