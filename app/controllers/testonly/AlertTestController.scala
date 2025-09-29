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
import controllers.actions.{ApiAuthenticatedIdentifierAction, IdentifierAction}
import models.grs.create.NewJourneyResponse
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.IdentifierGenerator
import views.html.testonly.StubGrsView

import java.util.UUID
import javax.inject.Inject

class AlertTestController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    val controllerComponents: MessagesControllerComponents,
) extends FrontendBaseController
    with Logging
    with I18nSupport {

  val delay = 310000

  def simulateError(errorType: String): Action[AnyContent] = identify {
    errorType match {
      case "503" =>
        logger.error(s"Test alert: simulated service unavailable error")
        ServiceUnavailable("Simulated service failure")
      case "500" =>
        logger.error("Test alert: Simulated 500 error")
        throw new RuntimeException("Sumulated internal server error")
      case "404" =>
        NotFound("Test alert: Simulated 404 error")
      case "timeout" =>
        Thread.sleep(delay)
        Ok("Simulated delayed response")
      case _ =>
        BadRequest(s"Unknown error type: $errorType")
    }
  }
}
