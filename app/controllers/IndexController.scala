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
import models.UserAnswers
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.*
import repositories.SessionRepository
import services.SignUpService.SignUpResult
import services.{DashboardService, SignUpService}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DashboardView

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

import javax.inject.Inject

class IndexController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    val controllerComponents: MessagesControllerComponents,
    view: DashboardView,
    dashboardService: DashboardService,
    signUpService: SignUpService,
    repository: SessionRepository
)(using ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData) { implicit request =>
    val currentStage = dashboardService.deriveCurrentStage(request.userAnswers)
    Ok(view(currentStage))
  }

  def submit: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    for {
      response <- submitSignUp(request.userAnswers)
      _        <- repository
        .clear(request.userId)
        .recover { case NonFatal(e) =>
          logger.warn("[PostSignUp][CLEAR_MONGO_FAIL]", e)
          false
        }
    } yield response
  }

  private def submitSignUp[A](userAnswers: UserAnswers)(using Request[A]) = for {
    result <- signUpService.submit(userAnswers)
  } yield result match {
    case SignUpResult.Success(_) =>
      Redirect(routes.RegistrationCompleteController.onPageLoad)
    case SignUpResult.InsufficientUserAnswers =>
      logger.warn("[PostSignUp][INSUFFICENT_USER_ANSWERS]")
      throw new InternalServerException("Unable to create PostSignUp Request")
    case SignUpResult.BadRequestFailure =>
      logger.warn("[PostSignUp][BAD_REQUEST]")
      throw new InternalServerException("PostSignUp returned BAD_REQUEST")
    case SignUpResult.MalformedResponse =>
      logger.warn("[PostSignUp][MalformedResponse]")
      throw new InternalServerException("PostSignUp returned a MalformedResponse")
    case SignUpResult.ProtectedServiceFailure(status) =>
      logger.warn(s"[PostSignUp][PROTECTED_SERVICE_FAILURE]status=$status")
      throw new InternalServerException(s"PostSignUp returned $status")
    case SignUpResult.UnknownFailure(status) =>
      logger.warn(s"[PostSignUp][Unknown]status=$status")
      throw new InternalServerException(s"PostSignUp returned an unknown status=$status")
  }

}
