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

class SignOutISpec extends ISpecBase {

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  "/account/sign-out must redirect to sign out" - {
    val targetUrl = s"$baseUrl/senior-accounting-officer/registration/account/sign-out"

    "when Auth is missing" in {
      MockAuthHelper.mockAuthOk()

      val response =
        wsClient
          .url(targetUrl)
          .get()
          .futureValue

      MockAuthHelper.verifyAuthWasCalled(times = 0)
      response.status mustBe Status.SEE_OTHER
      response.headers("Location").head must startWith(appConfig.signOutUrl)
    }

    "Auth is successful" in {
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
      response.status mustBe Status.SEE_OTHER
      response.headers("Location").head must startWith(appConfig.signOutUrl)
    }

    "the user has an Individual affinity group" in {
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
      response.headers("Location").head must startWith(appConfig.signOutUrl)
    }

    "the user has an Agent affinity group" in {
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
      response.headers("Location").head must startWith(appConfig.signOutUrl)
    }

    "the user has an Organisation affinity group with Assistant credential role" in {
      MockAuthHelper.mockAuthStandardUser()

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
      response.headers("Location").head must startWith(appConfig.signOutUrl)
    }

    "the user already holds the DSAO enrolment" in {
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
      response.headers("Location").head must startWith(appConfig.signOutUrl)
    }

    "Auth did not respond with the required retrievals must" in {
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
      response.status mustBe Status.SEE_OTHER
      response.headers("Location").head must startWith(appConfig.signOutUrl)
    }
  }

  "/account/account/sign-out-survey must redirect to sign out" - {
    val targetUrl = s"$baseUrl/senior-accounting-officer/registration/account/sign-out-survey"

    "when Auth is missing" in {
      MockAuthHelper.mockAuthOk()

      val response =
        wsClient
          .url(targetUrl)
          .get()
          .futureValue

      MockAuthHelper.verifyAuthWasCalled(times = 0)
      response.status mustBe Status.SEE_OTHER
      response.headers("Location").head must startWith(appConfig.signOutUrl)
    }

    "Auth is successful" in {
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
      response.status mustBe Status.SEE_OTHER
      response.headers("Location").head must startWith(appConfig.signOutUrl)
    }

    "the user has an Individual affinity group" in {
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
      response.headers("Location").head must startWith(appConfig.signOutUrl)
    }

    "the user has an Agent affinity group" in {
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
      response.headers("Location").head must startWith(appConfig.signOutUrl)
    }

    "the user has an Organisation affinity group with Assistant credential role" in {
      MockAuthHelper.mockAuthStandardUser()

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
      response.headers("Location").head must startWith(appConfig.signOutUrl)
    }

    "the user already holds the DSAO enrolment" in {
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
      response.headers("Location").head must startWith(appConfig.signOutUrl)
    }

    "Auth did not respond with the required retrievals must" in {
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
      response.status mustBe Status.SEE_OTHER
      response.headers("Location").head must startWith(appConfig.signOutUrl)
    }
  }

}
