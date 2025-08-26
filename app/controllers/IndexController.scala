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

package controllers

import controllers.actions.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DashboardView

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import models.UserAnswers
import pages.CompanyDetailsPage
import pages.ContactsPage
import pages.SubmitAnswersPage

class IndexController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    val controllerComponents: MessagesControllerComponents,
    view: DashboardView
)(using ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData) { implicit request =>
    Ok(view(DashboardState.CompanyDetails)) // getDashboardState(request.userAnswers)
  }

  def continue: Action[AnyContent] = (identify andThen getData) { implicit request =>
    Redirect(routes.CheckYourAnswersController.onPageLoad())
  }

  private def getDashboardState(userAnswers: Option[UserAnswers]): DashboardState =
  {
    userAnswers.fold(DashboardState.CompanyDetails) { userAnswer =>
      checks.collectFirst { case (pred, state) if pred(userAnswer) => state }
        .getOrElse(DashboardState.AnswersSubmitted)
    }
  }

  private val checks: List[(UserAnswers => Boolean, DashboardState)] = List(
  (_.get(CompanyDetailsPage).isEmpty,   DashboardState.CompanyDetails),
  (_.get(ContactsPage).isEmpty,         DashboardState.ContactDetails),
  (_.get(SubmitAnswersPage).isEmpty,    DashboardState.SubmitYourAnswers)
)

}

enum DashboardState {
  case CompanyDetails,
    ContactDetails,
    SubmitYourAnswers,
    AnswersSubmitted
}
