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

import com.github.tomakehurst.wiremock.client.WireMock.*
import routes.SignUpISpec.*
import play.api.http.HeaderNames
import play.api.http.Status
import play.api.libs.json.Json
import play.api.libs.ws.writeableOf_JsValue
import support.{ISpecBase, MockAuthHelper}

class SignUpISpec extends ISpecBase {

  override def additionalConfigs: Map[String, Any] = Map(
    "microservice.services.etmp-subscription-api.port"                -> wireMockPort,
    "microservice.services.etmp-subscription-api.path"                -> "/etmp/subscriptions",
    "microservice.services.etmp-subscription-api.authorization-token" -> "test-token"
  )

  private val targetUrl = s"$baseUrl/senior-accounting-officer/registration/sign-up"

  "POST /sign-up" - {
    "must call ETMP and return the subscription ID" in {
      MockAuthHelper.mockAuthOk()
      mockEtmpSubscription()

      val response =
        wsClient
          .url(targetUrl)
          .withHttpHeaders(
            HeaderNames.AUTHORIZATION -> "Bearer mock-bearer-token",
            "Csrf-Token"       -> "nocheck"
          )
          .post(validSignUpRequestJson)
          .futureValue

      response.status mustBe Status.CREATED
      response.json mustBe Json.obj("saoSubscriptionId" -> testSubscriptionId)
      verify(postRequestedFor(urlEqualTo("/etmp/subscriptions")))
    }
  }
}

object SignUpISpec {
  private val testSubscriptionId = "XE0001234567890"
  private val testUtr            = "1234567890"

  private val validSignUpRequestJson = Json.obj(
    "idType"   -> "UTR",
    "idNumber" -> testUtr
  )

  private def mockEtmpSubscription() =
    stubFor(
      post(urlEqualTo("/etmp/subscriptions"))
        .willReturn(
          aResponse()
            .withHeader("content-type", "application/json")
            .withBody(Json.obj("saoSubscriptionId" -> testSubscriptionId).toString)
            .withStatus(Status.CREATED)
        )
    )
}
