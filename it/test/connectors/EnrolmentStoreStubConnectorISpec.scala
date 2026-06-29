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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock.*
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.Request
import play.api.test.FakeRequest
import support.ISpecBase

class EnrolmentStoreStubConnectorISpec extends ISpecBase {

  override def additionalConfigs: Map[String, Any] =
    Map("microservice.services.enrolment-store-stub.port" -> wireMockPort)

  private val connector = app.injector.instanceOf[EnrolmentStoreStubConnector]
  private val groupId   = "sao-registration-test-provider-id"
  private val persona   = Json.obj("groupId" -> groupId)

  given Request[?] = FakeRequest()

  "EnrolmentStoreStubConnector.upsertGroupPersona" - {

    "must post a new group persona when the group does not exist" in {
      stubFor(get(urlEqualTo(s"/enrolment-store-stub/data/group/$groupId")).willReturn(aResponse().withStatus(Status.NOT_FOUND)))
      stubFor(post(urlEqualTo("/enrolment-store-stub/data")).willReturn(aResponse().withStatus(Status.NO_CONTENT)))

      val result = connector.upsertGroupPersona(groupId, persona).futureValue

      result mustBe true
      verify(1, postRequestedFor(urlEqualTo("/enrolment-store-stub/data")).withRequestBody(equalToJson(persona.toString)))
    }

    "must update an existing group persona when the group exists" in {
      stubFor(get(urlEqualTo(s"/enrolment-store-stub/data/group/$groupId")).willReturn(aResponse().withStatus(Status.OK)))
      stubFor(
        put(urlEqualTo(s"/enrolment-store-stub/data/group/$groupId")).willReturn(aResponse().withStatus(Status.NO_CONTENT))
      )

      val result = connector.upsertGroupPersona(groupId, persona).futureValue

      result mustBe true
      verify(
        1,
        putRequestedFor(urlEqualTo(s"/enrolment-store-stub/data/group/$groupId")).withRequestBody(equalToJson(persona.toString))
      )
    }

    "must return false when the stub does not accept the persona" in {
      stubFor(get(urlEqualTo(s"/enrolment-store-stub/data/group/$groupId")).willReturn(aResponse().withStatus(Status.NOT_FOUND)))
      stubFor(post(urlEqualTo("/enrolment-store-stub/data")).willReturn(aResponse().withStatus(Status.INTERNAL_SERVER_ERROR)))

      connector.upsertGroupPersona(groupId, persona).futureValue mustBe false
    }
  }
}
