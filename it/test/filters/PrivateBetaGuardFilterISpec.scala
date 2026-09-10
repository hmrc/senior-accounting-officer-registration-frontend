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

package filters

import config.{AppConfig, FeatureToggleSupport}
import models.config.FeatureToggle.PrivateBeta
import org.scalatest.concurrent.Eventually
import play.api.http.HeaderNames
import support.MockAuthHelper.authSession
import support.SessionCookieBaker.bakeSessionCookie
import support.{ISpecBase, MockAuthHelper, SessionCookieBaker}

class PrivateBetaGuardFilterISpec extends ISpecBase with FeatureToggleSupport with Eventually {

  private val appConfig = app.injector.instanceOf[AppConfig]

  override def beforeEach(): Unit = {
    super.beforeEach()
    MockAuthHelper.mockAuthOk()
  }

  "PrivateBetaGuard" - {
    "when the features.private-beta is enabled" - {

      "must pass the private beta guard filter" - {
        "when the proof of matching password is in session" in {
          enable(PrivateBeta)
          eventually(appConfig.privateBetaModeEnabled mustBe true)

          val response =
            wsClient
              .url(s"$baseUrl/")
              .withHttpHeaders(
                HeaderNames.COOKIE -> bakeSessionCookie(authSession + ("SAOPrivateBeta" -> "true")),
                "Csrf-Token"       -> "nocheck"
              )
              .get()
              .futureValue

          response.status must not be 303
        }

        "when the route is allow listed" in {
          enable(PrivateBeta)
          eventually(appConfig.privateBetaModeEnabled mustBe true)

          val response =
            wsClient
              .url(s"$baseUrl/ping/ping")
              .withHttpHeaders(
                HeaderNames.COOKIE -> bakeSessionCookie(authSession),
                "Csrf-Token"       -> "nocheck"
              )
              .get()
              .futureValue

          response.status must not be 303
        }

        "when the route has a modifier to ignore the filter" in {
          enable(PrivateBeta)
          eventually(appConfig.privateBetaModeEnabled mustBe true)

          val response =
            wsClient
              .url(s"$baseUrl/senior-accounting-officer/private-beta")
              .withHttpHeaders(
                HeaderNames.COOKIE -> bakeSessionCookie(authSession),
                "Csrf-Token"       -> "nocheck"
              )
              .get()
              .futureValue

          response.status must not be 303
        }
      }

      "must redirect the user to beta login page" - {
        "when the proof of matching password is not in session" in {
          enable(PrivateBeta)
          eventually(appConfig.privateBetaModeEnabled mustBe true)

          val response =
            wsClient
              .url(s"$baseUrl/")
              .withHttpHeaders(
                HeaderNames.COOKIE -> bakeSessionCookie(authSession),
                "Csrf-Token"       -> "nocheck"
              )
              .get()
              .futureValue

          response.status mustBe 303
          response.header(HeaderNames.LOCATION).value mustBe controllers.beta.routes.BetaLoginController
            .onPageLoad()
            .url
        }
      }

    }

    "when the features.private-beta is disabled" - {

      "must pass the private beta guard filter" - {
        "when the proof of matching password is in session" in {
          disable(PrivateBeta)
          eventually(appConfig.privateBetaModeEnabled mustBe false)

          val response =
            wsClient
              .url(s"$baseUrl/")
              .withHttpHeaders(
                HeaderNames.COOKIE -> bakeSessionCookie(authSession + ("SAOPrivateBeta" -> "true")),
                "Csrf-Token"       -> "nocheck"
              )
              .get()
              .futureValue

          response.status must not be 303
        }

        "when the proof of matching password is not in session" in {
          disable(PrivateBeta)
          eventually(appConfig.privateBetaModeEnabled mustBe false)

          val response =
            wsClient
              .url(s"$baseUrl/")
              .withHttpHeaders(
                HeaderNames.COOKIE -> bakeSessionCookie(authSession),
                "Csrf-Token"       -> "nocheck"
              )
              .get()
              .futureValue

          response.status must not be 303
        }
      }

    }

  }

}
