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
import controllers.beta.actions.{FakePrivateBetaIdentifierAction, PrivateBetaIdentifierAction}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.api.{Application, inject}
import views.html.beta.GuidanceSubmitView

class GuidanceSubmitControllerSpec extends SpecBase with GuiceOneAppPerSuite {

  override def fakeApplication(): Application = {
    new GuiceApplicationBuilder()
      .overrides(
        inject.bind[PrivateBetaIdentifierAction].to[FakePrivateBetaIdentifierAction]
      )
      .build()
  }

  "GuidanceSubmit Controller" - {

    "must return OK and the correct view for a GET" in {
      val request = FakeRequest(GET, routes.GuidanceSubmitController.onPageLoad().url)

      val result = route(app, request).value

      val view = app.injector.instanceOf[GuidanceSubmitView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view()(using request, messages(app)).toString
    }
  }
}
