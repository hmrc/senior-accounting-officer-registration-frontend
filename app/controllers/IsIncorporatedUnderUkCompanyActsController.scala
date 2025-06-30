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
import forms.IsIncorporatedUnderUkCompanyActsFormProvider

import javax.inject.Inject
import models.{NormalMode, UserAnswers}
import navigation.Navigator
import pages.IsIncorporatedUnderUkCompanyActsPage
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.IsIncorporatedUnderUkCompanyActsView

import scala.concurrent.{ExecutionContext, Future}

class IsIncorporatedUnderUkCompanyActsController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: Navigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    formProvider: IsIncorporatedUnderUkCompanyActsFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: IsIncorporatedUnderUkCompanyActsView
)(using ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData) { implicit request =>

    val preparedForm = request.userAnswers.flatMap(_.get(IsIncorporatedUnderUkCompanyActsPage)) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        value =>
          val logger = Logger(this.getClass).logger
          logger.warn(s"value ${value}")
          val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.userId))
          for {
            updatedAnswers <- Future.fromTry(userAnswers.set(IsIncorporatedUnderUkCompanyActsPage, value))
            _ = logger.warn(s"updatedAnswers ${updatedAnswers}")
            _ <- sessionRepository.set(updatedAnswers)
            _ = logger.warn(s"updatedAnswers updated")
          } yield Redirect(navigator.nextPage(IsIncorporatedUnderUkCompanyActsPage, NormalMode, updatedAnswers))
      )
  }
}
