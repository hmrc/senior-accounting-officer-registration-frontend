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

import base.SpecBase
import models.grs.create.NewJourneyResponse
import models.grs.retrieve.CompanyDetails
import org.scalatest.matchers.{BePropertyMatchResult, BePropertyMatcher}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.libs.json.JsValue
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.testonly.StubGrsView

import java.util.UUID

import GrsStubsControllerSpec.*

class GrsStubsControllerSpec extends SpecBase with GuiceOneAppPerSuite {

  override def fakeApplication(): Application =
    applicationBuilder(userAnswers = Some(emptyUserAnswers))
      .configure(Map("application.router" -> "testOnlyDoNotUseInAppConf.Routes"))
      .build()

  val testJourneyId: String = UUID.randomUUID().toString

  "GrsStubsController.startGrs" - {
    "must return 201 with a valid NewJourneyResponse" in {
      val request = FakeRequest(POST, controllers.testonly.routes.GrsStubsController.startGrs().url)

      val result = route(app, request).value

      status(result) mustEqual CREATED
      contentAsJson(result) mustBe a(newJourneyResponse)
    }
  }

  "GrsStubsController.getStubGrs" - {
    "must return 200 with the correct view" in {
      val request = FakeRequest(GET, controllers.testonly.routes.GrsStubsController.getStubGrs(testJourneyId).url)

      val result = route(app, request).value

      val view = app.injector.instanceOf[StubGrsView]

      status(result) mustEqual OK
      contentAsString(result) mustBe view(testJourneyId)(request, messages(app)).toString
    }
  }

  "GrsStubsController.postStubGrs" - {
    "must return 303 to the callback url" in {
      val request = FakeRequest(POST, controllers.testonly.routes.GrsStubsController.postStubGrs(testJourneyId).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      headers(result)
        .get("Location")
        .value mustBe s"http://localhost:10057/senior-accounting-officer/registration/business-match/result?journeyId=$testJourneyId"
    }
  }

  "GrsStubsController.getGrs" - {
    "must return the stubbed response" in {
      val request = FakeRequest(GET, controllers.testonly.routes.GrsStubsController.getGrs(testJourneyId).url)

      val result = route(app, request).value

      status(result) mustEqual OK
      contentAsJson(result) mustBe a(companyDetailsResponse)
    }
  }

}

object GrsStubsControllerSpec {
  val newJourneyResponse: BePropertyMatcher[JsValue] = (sut: JsValue) =>
    BePropertyMatchResult(!sut.validate[NewJourneyResponse].isError, "NewJourneyResponse")

  val companyDetailsResponse: BePropertyMatcher[JsValue] = (sut: JsValue) =>
    BePropertyMatchResult(!sut.validate[CompanyDetails].isError, "CompanyDetails")
}
