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

import base.SpecBase
import controllers.RegistrationCompleteControllerSpec.*
import controllers.actions.EnrolmentRequiredAction
import models.registration.RegistrationCompleteDetails
import models.requests.EnroledRequest
import org.apache.pekko.stream.testkit.NoMaterializer
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.*
import play.api.test.Helpers.*
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.http.UnauthorizedException
import views.html.RegistrationCompleteView

import scala.annotation.targetName
import scala.concurrent.{ExecutionContext, Future}

import java.time.*

class RegistrationCompleteControllerSpec extends SpecBase {

  @targetName("enroledApplicationBuilder")
  def applicationBuilder(enrolment: Option[String]): GuiceApplicationBuilder =
    super
      .applicationBuilder(userAnswers = None)
      .overrides(
        bind[EnrolmentRequiredAction]
          .toInstance(new FakeEnrolmentRequiredAction(enrolment))
      )

  lazy val registrationCompleteRoute: String = routes.RegistrationCompleteController.onPageLoad.url

  "RegistrationComplete Controller" - {

    "must return OK and the correct view for a GET when company details are available and host is not localhost" in {

      val testRegistrationId = "XMPLR0123456789"

      val application = applicationBuilder(enrolment = Some(testRegistrationId))
        .configure(Map("hub-frontend.host" -> "xyz"))
        .overrides(bind[Clock].to[MockClock])
        .build()

      running(application) {
        val request = FakeRequest(GET, registrationCompleteRoute)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[RegistrationCompleteView]

        val registrationData = RegistrationCompleteDetails(
          registrationId = testRegistrationId
        )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(registrationData)(using
          request,
          messages(application)
        ).toString
      }
    }
  }
}

object RegistrationCompleteControllerSpec {

  class MockClock extends Clock {
    override def instant(): Instant = {
      LocalDateTime.of(2025, 1, 17, 11, 30).toInstant(ZoneOffset.UTC)
    }

    override def withZone(zone: ZoneId): Clock = ???

    override def getZone(): ZoneId = ZoneOffset.UTC
  }

  class FakeEnrolmentRequiredAction(enrolment: Option[String]) extends EnrolmentRequiredAction {
    override def invokeBlock[A](request: Request[A], block: EnroledRequest[A] => Future[Result]): Future[Result] =
      enrolment match {
        case Some(subscriptionId) => block(EnroledRequest(request, subscriptionId))
        case _                    => Future.failed(new UnauthorizedException("No active HMRC-DSAO-ORG enrolment"))
      }

    override def parser: BodyParser[AnyContent] = Helpers.stubPlayBodyParsers(NoMaterializer).default

    override protected def executionContext: ExecutionContext = ExecutionContext.global
  }

}
