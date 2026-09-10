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

package controllers.beta.actions

import com.google.inject.Inject
import config.AppConfig
import controllers.beta.actions.AuthenticatedIdentifierAction.DsaoEnrolmentKey
import controllers.routes
import models.requests.IdentifierRequest
import play.api.mvc.Results.*
import play.api.mvc.{ActionFunction, *}
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait PrivateBetaIdentifierAction
    extends ActionBuilder[IdentifierRequest, AnyContent]
    with ActionFunction[Request, IdentifierRequest]

class PrivateBetaIdentifierActionImpl @Inject() (
    config: AppConfig,
    val parser: BodyParsers.Default,
    override val authConnector: AuthConnector
)(using override val executionContext: ExecutionContext)
    extends PrivateBetaIdentifierAction
    with AuthorisedFunctions {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {
    given hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised().retrieve(Retrievals.internalId and Retrievals.allEnrolments) {
      case _ ~ enrolments if isAlreadyRegistered(enrolments) =>
        Future.successful(Redirect(routes.AlreadyRegisteredController.onPageLoad()))
      case internalId ~ _ =>
        internalId
          .map { id =>
            block(IdentifierRequest(request, id))
          }
          .getOrElse(throw new UnauthorizedException("Unable to retrieve internal Id"))
    } recover {
      case _: NoActiveSession =>
        Redirect(
          config.loginUrl,
          Map("continue" -> Seq(config.loginContinueUrl.replace("registration", "private-beta")))
        )
      case _: AuthorisationException =>
        Redirect(routes.UnauthorisedController.onPageLoad())
    }
  }

  private def isAlreadyRegistered(enrolments: Enrolments): Boolean =
    enrolments.enrolments.exists(_.key == DsaoEnrolmentKey)

}

object AuthenticatedIdentifierAction {
  val DsaoEnrolmentKey = "HMRC-DSAO-ORG"
}
