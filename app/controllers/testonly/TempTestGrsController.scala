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

package controllers.testonly

import config.FrontendAppConfig
import connectors.GrsConnector
import controllers.actions.IdentifierAction
import models.grs.create.{NewJourneyRequest, NewJourneyResponse, ServiceLabels}
import models.grs.retrieve.CompanyDetails
import play.api.Logger
import play.api.i18n.{I18nSupport, Lang, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.hmrcfrontend.config.AccessibilityStatementConfig

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TempTestGrsController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    grsConnector: GrsConnector,
    val controllerComponents: MessagesControllerComponents,
    appConfig: FrontendAppConfig,
    accessibilityStatementConfig: AccessibilityStatementConfig
)(using ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def get(): Action[AnyContent] = identify.async { implicit request =>
    val continueUrl =
      controllers.testonly.routes.TempTestGrsController.callBack("").absoluteURL().replaceAll("\\?.*$", "")

    val grsStartRequest = NewJourneyRequest(
      continueUrl = continueUrl,
      businessVerificationCheck = false,
      deskProServiceId = appConfig.contactFormServiceIdentifier,
      signOutUrl = controllers.auth.routes.AuthController.signOut().absoluteURL(),
      regime = "VATC",
      accessibilityUrl = accessibilityStatementConfig.url.get,
      labels = Some(ServiceLabels(en = messagesApi.preferred(Seq(Lang("en"))).messages("service.name")))
    )

    for {
      r <- grsConnector.start(grsStartRequest)
    } yield SeeOther(Json.parse(r.body).as[NewJourneyResponse].journeyStartUrl)
  }

  def callBack(journeyId: String): Action[AnyContent] = identify.async { implicit request =>
    for {
      r <- grsConnector.retrieve(journeyId)
      if (!Json.parse(r.body).validate[CompanyDetails].isError)
    } yield Ok(Json.parse(r.body))
  }

}
