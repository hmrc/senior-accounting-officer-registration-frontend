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
import play.api.i18n.Lang.logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SignUpService
import services.SignUpService.SignUpResult
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.RegistrationPendingView

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject

class RegistrationPendingController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    val controllerComponents: MessagesControllerComponents,
    view: RegistrationPendingView,
    signUpService: SignUpService
)(using ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(idempotencyKey: String): Action[AnyContent] =
    (identify andThen getData).async { implicit request =>
      signUpService.getRegistrationState(idempotencyKey).flatMap {
        case SignUpResult.Success(subscriptionId) =>
          Future.successful(Redirect(routes.RegistrationCompleteController.onPageLoad))
        case SignUpResult.Pending(idempotencyKey) =>
          Future.successful(Ok(view()))
        case SignUpResult.InsufficientUserAnswers =>
          logger.warn("[getRegistrationState][INSUFFICENT_USER_ANSWERS]")
          throw new InternalServerException("Unable to create getRegistrationState Request")
        case SignUpResult.BadRequestFailure =>
          logger.warn("[getRegistrationState][BAD_REQUEST]")
          throw new InternalServerException("getRegistrationState returned BAD_REQUEST")
        case SignUpResult.MalformedResponse =>
          logger.warn("[getRegistrationState][MalformedResponse]")
          throw new InternalServerException("getRegistrationState returned a MalformedResponse")
        case SignUpResult.ProtectedServiceFailure(status) =>
          logger.warn(s"[getRegistrationState][PROTECTED_SERVICE_FAILURE]status=$status")
          throw new InternalServerException(s"getRegistrationState returned $status")
        case SignUpResult.UnknownFailure(status) =>
          logger.warn(s"[getRegistrationState][Unknown]status=$status")
          throw new InternalServerException(s"getRegistrationState returned an unknown status=$status")
      }
    }
}
