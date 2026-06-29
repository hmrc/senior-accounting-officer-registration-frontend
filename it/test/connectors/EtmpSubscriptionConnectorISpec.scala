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

import com.github.tomakehurst.wiremock.client.WireMock.*
import connectors.EtmpSubscriptionConnectorISpec.*
import models.signup.{EtmpSubscriptionRequest, EtmpSubscriptionResponse}
import org.scalatest.EitherValues
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.Request
import play.api.test.FakeRequest
import support.ISpecBase

class EtmpSubscriptionConnectorISpec extends ISpecBase with EitherValues {

  override def additionalConfigs: Map[String, Any] = Map(
    "microservice.services.etmp-subscription-api.port"                -> wireMockPort,
    "microservice.services.etmp-subscription-api.path"                -> "/etmp/subscriptions",
    "microservice.services.etmp-subscription-api.authorization-token" -> "test-token"
  )

  val SUT          = app.injector.instanceOf[EtmpSubscriptionConnector]
  given Request[?] = FakeRequest()

  "EtmpSubscriptionConnector.subscribe" - {
    "must post the expected request to ETMP and return the subscription ID" in {
      mockEtmpSubscription()

      val result = SUT.subscribe(EtmpSubscriptionRequest("UTR", testUtr)).futureValue

      result.value mustBe EtmpSubscriptionResponse(testSubscriptionId)

      verify(
        postRequestedFor(urlEqualTo("/etmp/subscriptions"))
          .withHeader("Authorization", equalTo("Basic test-token"))
          .withHeader("X-Transmitting-System", equalTo("HIP"))
          .withHeader("X-Originating-System", equalTo("MDTP"))
          .withHeader("correlationid", matching("[0-9a-fA-F-]{36}"))
          .withHeader("X-Receipt-Date", matching("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z"))
          .withRequestBody(equalTo(Json.obj("idType" -> "UTR", "idNumber" -> testUtr).toString))
      )
    }

    "must return Left when ETMP returns a non-201 response" in {
      mockEtmpSubscription(status = Status.INTERNAL_SERVER_ERROR)

      SUT.subscribe(EtmpSubscriptionRequest("UTR", testUtr)).futureValue mustBe a[Left[?, ?]]
    }

    "must return Left when ETMP returns a 201 response with an invalid body" in {
      mockEtmpSubscription(body = Json.obj("unexpected" -> "value").toString)

      SUT.subscribe(EtmpSubscriptionRequest("UTR", testUtr)).futureValue mustBe a[Left[?, ?]]
    }
  }
}

object EtmpSubscriptionConnectorISpec {
  val testSubscriptionId = "XE0001234567890"
  val testUtr            = "1234567890"

  def mockEtmpSubscription(
      status: Int = Status.CREATED,
      body: String = Json.obj("saoSubscriptionId" -> testSubscriptionId).toString
  ) =
    stubFor(
      post(urlEqualTo("/etmp/subscriptions"))
        .willReturn(
          aResponse()
            .withHeader("content-type", "application/json")
            .withBody(body)
            .withStatus(status)
        )
    )
}
