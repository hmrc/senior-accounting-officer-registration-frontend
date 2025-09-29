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
import org.apache.pekko
import org.apache.pekko.actor.ActorSystem
import play.api.Logging
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future, Promise}

import javax.inject.Inject

class AlertTestController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    val controllerComponents: MessagesControllerComponents,
    actorSystem: ActorSystem
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with Logging {

  val delay = 310000

  def simulateError(errorType: String): Action[AnyContent] = identify.async {
    errorType match {
      case "500" =>
        logger.warn(s"Test alert: simulated Internal server error (500)")
        Future.successful(InternalServerError("Simulated service failure"))
      case "503" =>
        logger.error(s"Test alert: simulated server error (503)")
        Future.successful(ServiceUnavailable("Simulated service failure"))
      case "404" =>
        Future.successful(NotFound("Test alert: Simulated 404 error"))
      case "timeout-long" =>
        raceWithTimeout(slowOperation(35.seconds), 30.seconds)
          .map { result =>
            Ok(s"Got result: $result")
          }
          .recover { case e: scala.concurrent.TimeoutException =>
            logger.error("TEST ALERT: Request timed out after 30 seconds")
            GatewayTimeout("simulated upstream timeout")
          }
      case "slow-response" =>
        logger.warn("TEST ALERT: slow response initiated")
        slowOperation(5.seconds)
          .map { result =>
            logger.warn("TEST ALERT: slow response completed")
            Ok(s"Slow response: $result")
          }

      case _ =>
        Future.successful(BadRequest(s"Unknown error type: $errorType"))
    }
  }

  // simulated slow operation
  private def slowOperation(duration: FiniteDuration): Future[String] = {
    val promise = Promise[String]()
    actorSystem.scheduler.scheduleOnce(duration) {
      promise.success(s"Operation successfully completed after ${duration.toSeconds}s")
    }
    promise.future
  }

  // create a race condition between two futures
  private def raceWithTimeout[T](aFuture: Future[T], timeoutDuration: FiniteDuration): Future[T] = {
    val timeoutFuture = pekko.pattern.after(timeoutDuration, actorSystem.scheduler) {
      Future.failed(new scala.concurrent.TimeoutException(s"Operation timed out after $timeoutDuration"))
    }
    Future.firstCompletedOf(Seq(aFuture, timeoutFuture))
  }

}
