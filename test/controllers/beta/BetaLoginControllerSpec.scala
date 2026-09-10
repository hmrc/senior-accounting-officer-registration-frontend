/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.beta

import base.SpecBase
import config.AppConfig
import controllers.beta.actions.{FakePrivateBetaIdentifierAction, PrivateBetaIdentifierAction}
import forms.beta.BetaLoginFormProvider
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.beta.BetaLoginView

class BetaLoginControllerSpec extends SpecBase with MockitoSugar with GuiceOneAppPerSuite {

  def form: Form[String]   = app.injector.instanceOf[BetaLoginFormProvider].apply()
  def testPassword: String = app.injector.instanceOf[AppConfig].privateBetaPassword

  lazy val betaLoginRoute: String = routes.BetaLoginController.onPageLoad().url

  override def fakeApplication(): Application = {
    new GuiceApplicationBuilder()
      .overrides(
        bind[PrivateBetaIdentifierAction].to[FakePrivateBetaIdentifierAction]
      )
      .build()
  }

  "BetaLogin Controller" - {

    "must return OK and the correct view for a GET" in {
      val request = FakeRequest(GET, betaLoginRoute)

      val result = route(app, request).value

      val view = app.injector.instanceOf[BetaLoginView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(form)(using request, messages(app)).toString
    }

    "must redirect to the beta Guidance page when valid data is submitted" in {
      val request =
        FakeRequest(POST, betaLoginRoute)
          .withFormUrlEncodedBody(("value", testPassword))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.GuidanceRegisterController.onPageLoad().url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val request =
        FakeRequest(POST, betaLoginRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = app.injector.instanceOf[BetaLoginView]

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual view(boundForm)(using request, messages(app)).toString
    }

  }
}
