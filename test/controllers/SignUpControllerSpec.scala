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
import controllers.SignUpControllerSpec.*
import models.signup.{SignUpRequest, SignUpResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.MimeTypes.JSON
import play.api.inject
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.SignUpService
import uk.gov.hmrc.http.InternalServerException

import scala.concurrent.Future

class SignUpControllerSpec extends SpecBase with MockitoSugar {

  override def applicationBuilder(userAnswers: Option[models.UserAnswers] = None) =
    super
      .applicationBuilder(userAnswers)
      .overrides(
        inject.bind[SignUpService].toInstance(mock[SignUpService])
      )

  "SignUpController.signUp" - {
    "must return 201 with the subscription ID" in {
      val application = applicationBuilder().build()

      running(application) {
        val service = application.injector.instanceOf[SignUpService]

        when(service.signUp(any[SignUpRequest])(using any())).thenReturn(
          Future.successful(Right(SignUpResponse(testSubscriptionId)))
        )

        val result = route(application, jsonRequest(validSignUpRequestJson)).value

        status(result) mustBe CREATED
        contentAsJson(result) mustBe Json.obj("saoSubscriptionId" -> testSubscriptionId)
      }
    }

    "must return 400 for an invalid request body" in {
      val application = applicationBuilder().build()

      running(application) {
        val result = route(application, jsonRequest(Json.obj("idType" -> "UTR", "idNumber" -> "12345"))).value

        status(result) mustBe BAD_REQUEST
      }
    }

    "must return 502 when ETMP subscription fails" in {
      val application = applicationBuilder().build()

      running(application) {
        val service = application.injector.instanceOf[SignUpService]

        when(service.signUp(any[SignUpRequest])(using any())).thenReturn(
          Future.successful(Left(new InternalServerException("ETMP failed")))
        )

        val result = route(application, jsonRequest(validSignUpRequestJson)).value

        status(result) mustBe BAD_GATEWAY
      }
    }
  }

  private def jsonRequest(body: play.api.libs.json.JsObject) =
    FakeRequest(POST, routes.SignUpController.signUp().url)
      .withHeaders(CONTENT_TYPE -> JSON)
      .withJsonBody(body)
}

object SignUpControllerSpec {
  private val testSubscriptionId     = "XE0001234567890"
  private val testUtr                = "1234567890"
  private val validSignUpRequestJson = Json.obj(
    "idType"   -> "UTR",
    "idNumber" -> testUtr
  )
}
