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

import config.AppConfig
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
    repository: SessionRepository,
    appConfig: AppConfig
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
      response <-
        if appConfig.faultToleranceEnabled then submitSignUpWithFaultTolerance(request.userAnswers)
        else submitSignUp(request.userAnswers)
      _ <- repository
        .clear(request.userId)
        .recover { case NonFatal(e) =>
          logger.warn("[PostSignUp][CLEAR_MONGO_FAIL]", e)
          false
        }
    } yield response
  }

  private def submitSignUp[A](userAnswers: UserAnswers)(using Request[A]) =
    signUpService.submit(userAnswers).map {
      case SignUpResult.Success(_) =>
        Redirect(routes.RegistrationCompleteController.onPageLoad)
      case result =>
        handleFailure(result, "PostSignUp")
    }

  private def submitSignUpWithFaultTolerance[A](userAnswers: UserAnswers)(using Request[A]) =
    signUpService.submitWithFaultTolerance(userAnswers).map {
      case SignUpResult.Pending(idempotencyKey) =>
        Redirect(routes.RegistrationRegisteringController.onPageLoad(idempotencyKey))
      case result =>
        handleFailure(result, "PostSignUpV2")
    }

  private def handleFailure(result: SignUpResult, context: String): Nothing =
    result match {
      case SignUpResult.InsufficientUserAnswers =>
        logger.warn(s"[$context][INSUFFICENT_USER_ANSWERS]")
        throw new InternalServerException(s"Unable to create $context Request")
      case SignUpResult.BadRequestFailure =>
        logger.warn(s"[$context][BAD_REQUEST]")
        throw new InternalServerException(s"$context returned BAD_REQUEST")
      case SignUpResult.MalformedResponse =>
        logger.warn(s"[$context][MalformedResponse]")
        throw new InternalServerException(s"$context returned a MalformedResponse")
      case SignUpResult.ProtectedServiceFailure(status) =>
        logger.warn(s"[$context][PROTECTED_SERVICE_FAILURE]status=$status")
        throw new InternalServerException(s"$context returned $status")
      case SignUpResult.UnknownFailure(status) =>
        logger.warn(s"[$context][Unknown]status=$status")
        throw new InternalServerException(s"$context returned an unknown status=$status")
      case _ =>
        throw new IllegalStateException(s"Unexpected success result for $context")
    }
}
