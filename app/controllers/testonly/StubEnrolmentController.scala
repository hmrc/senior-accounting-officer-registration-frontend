/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.testonly

import config.AppConfig
import connectors.EnrolmentStoreStubConnector
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.{Credentials, ~}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject

class StubEnrolmentController @Inject() (
    override val messagesApi: MessagesApi,
    val controllerComponents: MessagesControllerComponents,
    override val authConnector: AuthConnector,
    enrolmentStoreStubConnector: EnrolmentStoreStubConnector,
    appConfig: AppConfig
)(using ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with AuthorisedFunctions
    with Logging {

  def stubEnrolment(): Action[AnyContent] = Action.async { implicit request =>
    given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised()
      .retrieve(Retrievals.internalId and Retrievals.credentials) {
        case Some(_) ~ Some(credentials) =>
          val groupId = s"sao-registration-${credentials.providerId}"

          enrolmentStoreStubConnector.upsertGroupPersona(groupId, groupPersona(groupId, credentials)).map {
            case true  => Redirect(controllers.routes.IndexController.onPageLoad())
            case false => InternalServerError("Unable to configure enrolment-store-stub")
          }
        case _ =>
          Future.successful(InternalServerError("Unable to retrieve auth details"))
      }
      .recover {
        case _: NoActiveSession =>
          Redirect(
            appConfig.loginUrl,
            Map("continue" -> Seq(appConfig.prependHost(routes.StubEnrolmentController.stubEnrolment())))
          )
        case _: AuthorisationException =>
          Redirect(controllers.routes.UnauthorisedController.onPageLoad())
      }
  }

  private def groupPersona(groupId: String, credentials: Credentials): JsObject =
    Json.obj(
      "groupId"       -> groupId,
      "affinityGroup" -> "Organisation",
      "users"         -> Json.arr(
        Json.obj(
          "credId"         -> credentials.providerId,
          "name"           -> "SAO Test User",
          "email"          -> "sao-test-user@example.com",
          "credentialRole" -> "Admin",
          "description"    -> "SAO registration local test user"
        )
      ),
      "enrolments" -> Json.arr(
        Json.obj(
          "serviceName"           -> "HMRC-DSAO-ORG",
          "identifiers"           -> Json.arr(Json.obj("key" -> "EtmpSubscriptionId", "value" -> "1234567890")),
          "enrolmentFriendlyName" -> "HMRC DSAO Enrolment",
          "assignedUserCreds"     -> Json.arr(credentials.providerId),
          "state"                 -> "Activated",
          "enrolmentType"         -> "principal",
          "assignedToAll"         -> false
        )
      )
    )
}
