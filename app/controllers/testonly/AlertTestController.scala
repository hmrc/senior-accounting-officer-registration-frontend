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

import controllers.actions.IdentifierAction
import play.api.Logging
import play.api.libs.concurrent.Futures
import play.api.mvc.*
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject

class AlertTestController @Inject() (
    identify: IdentifierAction,
    val controllerComponents: MessagesControllerComponents,
    futures: Futures
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with Logging {

  def simulateError(errorType: String): Action[AnyContent] = identify.async {
    errorType match {
      case "500" =>
        logger.warn(s"Test alert: simulated Internal server error (500)")
        Future.successful(InternalServerError("Simulated service failure"))
      case "exception" =>
        logger.warn(s"Test alert: simulated runtime exception")
        throw new RuntimeException("Simulated runtime exception")
      case "slow-response" =>
        futures.delay(4.minutes).map { _ =>
          logger.warn(s"Test alert: slow response completed after 20s")
          Ok("Response completed after 4 minutes")
        }
      case "187" =>
        logger.warn(s"Test alert: simulated container kill")
        sys.exit(187)
      case "log-error" =>
        logger.error(s"Test alert: simulated error")
        Future.successful(Ok("Test alert: Simulated error log"))
      case _ =>
        Future.successful(BadRequest(s"Unknown error type: $errorType"))
    }
  }
}
