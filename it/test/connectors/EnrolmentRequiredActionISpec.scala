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

package connectors

import connectors.EnrolmentRequiredActionISpec.*
import controllers.actions.EnrolmentRequiredAction
import models.UserAnswers
import models.registration.NominatedCompany
import org.scalatest.BeforeAndAfterEach
import play.api.http.HeaderNames
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.DefaultBodyReadables.readableAsString
import play.api.mvc.{RequestHeader, Results}
import play.api.test.FakeRequest
import repositories.SessionRepository
import support.*
import support.MockAuthHelper.*
import views.html.ErrorTemplate

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}

class EnrolmentRequiredActionISpec extends ISpecBase {

  override def applicationBuilder: GuiceApplicationBuilder =
    GuiceApplicationBuilder()
      .overrides(bind[Clock].toInstance(stubClock))
      .appRoutes { app =>
        val enrolmentRequiredAction = app.injector.instanceOf[EnrolmentRequiredAction]

        { case ("GET", testPath) =>
          enrolmentRequiredAction { request =>
            Results.Ok(testSuccessBody(request.subscriptionId))
          }
        }
      }

  def repository: SessionRepository = app.injector.instanceOf[SessionRepository]

  def targetUrl = s"$baseUrl$testPath"

  "An endpoint with EnrolmentRequiredAction must" - {
    "pass the action successfully when" - {
      "the user has HMRC-DSAO-ORG enrolment" in {
        MockAuthHelper.mockAuthEnroled()

        val result = wsClient
          .url(targetUrl)
          .withHttpHeaders(
            HeaderNames.COOKIE -> SessionCookieBaker.bakeSessionCookie(authSession)
          )
          .get()
          .futureValue

        result.status mustBe 200
        result.body[String] mustBe testSuccessBody(testSubscriptionId)

        MockAuthHelper.verifyAuthWasCalled()
      }

    }

    "return 500 when" - {
      "the user does not have a HMRC-DSAO-ORG enrolment" in {
        MockAuthHelper.mockAuthNoEnrolments()

        val result = wsClient
          .url(targetUrl)
          .withHttpHeaders(
            HeaderNames.COOKIE -> SessionCookieBaker.bakeSessionCookie(authSession)
          )
          .get()
          .futureValue

        result.status mustBe 500
        result.body[String] mustBe default500ErrorTemplate
        
        MockAuthHelper.verifyAuthWasCalled()
      }
    }

  }

  def default500ErrorTemplate: String = {
    given Messages      = app.injector.instanceOf[MessagesApi].preferred(Seq.empty)
    given RequestHeader = FakeRequest("GET", testPath)
    val template        = app.injector.instanceOf[ErrorTemplate]
    template(
      Messages("global.error.InternalServerError500.title"),
      Messages("global.error.InternalServerError500.heading"),
      Messages("global.error.InternalServerError500.message")
    ).toString
  }
}

object EnrolmentRequiredActionISpec {
  private val instant          = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)
  val testPath                 = "/test-enrolment-required-action"

  def testSuccessBody(saoSubscriptionId: String) =
    s"Action Passed Successfully: $saoSubscriptionId"

}
