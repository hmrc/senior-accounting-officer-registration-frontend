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
import play.api.i18n.MessagesApi
import play.api.mvc.*
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future, Promise}

import java.util.{Timer, TimerTask}
import javax.inject.Inject

class AlertTestController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with Logging {

  val delayInSeconds = 4*60

  def simulateError(errorType: String): Action[AnyContent] = identify.async {
    errorType match {
      case "503" =>
        logger.warn(s"Test alert: simulated server error (503)")
        Future.successful(ServiceUnavailable("Simulated service failure"))
      case "500" =>
        logger.warn(s"Test alert: simulated Internal server error (500)")
        Future.successful(InternalServerError("Simulated service failure"))
      case "timeout" =>
        logger.warn(s"Test alert: simulated timeout")
        throw new RuntimeException("Simulated timeout exception")
      case "slow-response" =>
        logger.warn("TEST ALERT: slow response initiated")
        slowOperation((delayInSeconds))
          .map { result =>
            logger.warn("TEST ALERT: slow response completed")
            Ok(s"Slow response: $result")
          }
      case "187" =>
        logger.warn(s"Test alert: simulated container kill")
        sys.exit(187)
      case _ =>
        Future.successful(BadRequest(s"Unknown error type: $errorType"))
    }
  }

  // simulated slow operation
  private def slowOperation(seconds: Int): Future[Result] = {
    val promise = Promise[Result]()
    val timer   = new Timer()
    timer.schedule(
      new TimerTask {
        override def run(): Unit = {
          promise.success(Ok(s"Response completed after $seconds seconds"))
          timer.cancel()
        }
      },
      seconds * 1000L
    )
    promise.future
  }
}
