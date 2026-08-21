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

package routes

import config.AppConfig
import play.api.http.{HeaderNames, Status}
import support.MockAuthHelper.authSession
import support.{ISpecBase, MockAuthHelper, SessionCookieBaker}

class AuthActionISpec extends ISpecBase {

  val appConfig = app.injector.instanceOf[AppConfig]
  val targetUrl = s"$baseUrl/senior-accounting-officer/registration"

  "An endpoint with Auth Action when" - {
    "Auth is missing must" - {
      "respond with a 303 to login" in {
        MockAuthHelper.mockAuthOk()

        val response =
          wsClient
            .url(targetUrl)
            .get()
            .futureValue

        MockAuthHelper.verifyAuthWasCalled(times = 0)
        response.status mustBe Status.SEE_OTHER
        response.headers("Location").head must startWith(appConfig.loginUrl)
      }
    }

    "Auth is successful must" - {
      "respond with a 200" in {
        MockAuthHelper.mockAuthOk()

        val response =
          wsClient
            .url(targetUrl)
            .withHttpHeaders(
              HeaderNames.COOKIE -> SessionCookieBaker.bakeSessionCookie(authSession),
              "Csrf-Token"       -> "nocheck"
            )
            .get()
            .futureValue

        MockAuthHelper.verifyAuthWasCalled()
        response.status mustBe Status.OK

      }
    }

    "the user has an Individual affinity group must" - {
      "respond with a 303 to the cannot access service kick-out page" in {
        MockAuthHelper.mockAuthIndividual()

        val response =
          wsClient
            .url(targetUrl)
            .withFollowRedirects(false)
            .withHttpHeaders(
              HeaderNames.COOKIE -> SessionCookieBaker.bakeSessionCookie(authSession),
              "Csrf-Token"       -> "nocheck"
            )
            .get()
            .futureValue

        MockAuthHelper.verifyAuthWasCalled()
        response.status mustBe Status.SEE_OTHER
        response.headers("Location").head mustBe controllers.routes.CannotAccessServiceController.onPageLoad().url
      }
    }

    "the user has an Agent affinity group must" - {
      "respond with a 303 to the agent cannot access service kick-out page" in {
        MockAuthHelper.mockAuthAgent()

        val response =
          wsClient
            .url(targetUrl)
            .withFollowRedirects(false)
            .withHttpHeaders(
              HeaderNames.COOKIE -> SessionCookieBaker.bakeSessionCookie(authSession),
              "Csrf-Token"       -> "nocheck"
            )
            .get()
            .futureValue

        MockAuthHelper.verifyAuthWasCalled()
        response.status mustBe Status.SEE_OTHER
        response.headers("Location").head mustBe controllers.routes.AgentCannotAccessServiceController
          .onPageLoad()
          .url
      }
    }

    "the user already holds the DSAO enrolment must" - {
      "respond with a 303 to the already registered kick-out page" in {
        MockAuthHelper.mockAuthAlreadyEnroled()

        val response =
          wsClient
            .url(targetUrl)
            .withFollowRedirects(false)
            .withHttpHeaders(
              HeaderNames.COOKIE -> SessionCookieBaker.bakeSessionCookie(authSession),
              "Csrf-Token"       -> "nocheck"
            )
            .get()
            .futureValue

        MockAuthHelper.verifyAuthWasCalled()
        response.status mustBe Status.SEE_OTHER
        response.headers("Location").head mustBe controllers.routes.AlreadyRegisteredController.onPageLoad().url
      }
    }

    "Auth did not respond with the required retrievals must" - {
      "respond with a 500" in {
        MockAuthHelper.mockAuthNoId()

        val response =
          wsClient
            .url(targetUrl)
            .withHttpHeaders(
              HeaderNames.COOKIE -> SessionCookieBaker.bakeSessionCookie(authSession),
              "Csrf-Token"       -> "nocheck"
            )
            .get()
            .futureValue

        MockAuthHelper.verifyAuthWasCalled()
        response.status mustBe Status.INTERNAL_SERVER_ERROR
      }
    }
  }

}
