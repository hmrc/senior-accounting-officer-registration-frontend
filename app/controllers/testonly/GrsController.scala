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

import controllers.actions.{ApiAuthenticatedIdentifierAction, IdentifierAction}
import models.grs.create.NewJourneyResponse
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.testonly.StubGrsView

import java.util.UUID
import javax.inject.Inject

class GrsController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    apiIdentify: ApiAuthenticatedIdentifierAction,
    stubGrsView: StubGrsView,
    val controllerComponents: MessagesControllerComponents
) extends FrontendBaseController
    with I18nSupport {

  def startGrs(): Action[AnyContent] = apiIdentify { implicit request =>
    val uuid            = UUID.randomUUID()
    val journeyStartUrl = routes.GrsController.getStubGrs(uuid.toString).absoluteURL()

    Ok(Json.toJson(NewJourneyResponse(journeyStartUrl)))
  }

  def getStubGrs(journeyId: String): Action[AnyContent] = identify { implicit request =>
    Ok(stubGrsView(journeyId))
  }

  def postStubGrs(journeyId: String): Action[AnyContent] = identify { implicit request =>
    val redirectUrl = controllers.testonly.routes.TempTestGrsController.callBack(journeyId).absoluteURL()
    SeeOther(redirectUrl)
  }

  def getGrs(journeyId: String): Action[AnyContent] = apiIdentify { implicit request =>
    Ok(Json.parse("""
        |{"companyProfile":{"companyName":"Test Company Ltd","companyNumber":"AB123456","dateOfIncorporation":"2020-01-01","unsanitisedCHROAddress":{"address_line_1":"testLine1","address_line_2":"test town","care_of":"test name","country":"United Kingdom","locality":"test city","po_box":"123","postal_code":"AA11AA","premises":"1","region":"test region"}},"identifiersMatch":true,"registration":{"registrationStatus":"REGISTERED","registeredBusinessPartnerId":"X00000123456789"},"ctutr":"1234567890"}
        |""".stripMargin))
  }

}
