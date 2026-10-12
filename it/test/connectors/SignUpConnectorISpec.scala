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
import connectors.ProtectedServiceConnectorISpec.*
import models.registration.*
import support.ISpecBase
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import java.net.URI

class SignUpConnectorISpec extends ISpecBase {

  override def additionalConfigs: Map[String, Any] = Map(
    "microservice.services.senior-accounting-officer-registration.port" -> wireMockPort
  )

  lazy val SUT: SignUpConnector = app.injector.instanceOf[SignUpConnector]
  given HeaderCarrier           = HeaderCarrier()

  def submitUrl = "/senior-accounting-officer-registration/sign-up"
  def submitFaultToleranceUrl = "/senior-accounting-officer-registration/v2/sign-up"
  def getStateOfWorkUrl = "/senior-accounting-officer-registration/v2/sign-up/key"

  "A POST call from SignUpConnector.submit to the target URL" - {
    for status <- Seq(200, 400, 401, 500, 502) yield {
      s"must return the raw HttpResponse for status=$status" in {
        stubFor(
          post(urlEqualTo(submitUrl))
            .willReturn(
              aResponse()
                .withHeader("content-type", "application/json")
                .withBody(testBody)
                .withStatus(status)
            )
        )

        val result: HttpResponse =
          SUT
            .submit(
              SignUpRequest(
                etmpSafeId = "etmpSafeId",
                nominatedCompany = NominatedCompany(
                  name = "String",
                  utr = "String",
                  crn = "String"
                ),
                contacts = List(
                  Contact(
                    name = "String",
                    email = "String",
                    status = "String",
                    language = "String"
                  )
                ),
                idempotencyKey = None
              )
            )
            .futureValue

        result.status mustBe status
        result.body mustBe testBody

        verify(
          1,
          postRequestedFor(urlEqualTo(URI(submitUrl).getPath))
            .withRequestBody(equalToJson("""
              |{
              |  "etmpSafeId" : "etmpSafeId",
              |  "nominatedCompany" : {
              |    "name" : "String",
              |    "utr" : "String",
              |    "crn" : "String"
              |  },
              |  "contacts" : [ {
              |    "name" : "String",
              |    "email" : "String",
              |    "status" : "String",
              |    "language" : "String"
              |  } ]
              |}""".stripMargin))
        )
      }
    }
  }

  "A POST call from SignUpConnector.submitWithFaultTolerance to the target URL" - {
    for status <- Seq(202, 400, 401, 500, 502) yield {
      s"must return the raw HttpResponse for status=$status" in {
        stubFor(
          post(urlEqualTo(submitFaultToleranceUrl))
            .willReturn(
              aResponse()
                .withHeader("content-type", "application/json")
                .withBody(testBody)
                .withStatus(status)
            )
        )

        val result: HttpResponse =
          SUT
            .submitWithFaultTolerance(
              SignUpRequest(
                etmpSafeId = "etmpSafeId",
                nominatedCompany = NominatedCompany(
                  name = "String",
                  utr = "String",
                  crn = "String"
                ),
                contacts = List(
                  Contact(
                    name = "String",
                    email = "String",
                    status = "String",
                    language = "String"
                  )
                ),
                idempotencyKey = Some("key")
              )
            )
            .futureValue

        result.status mustBe status
        result.body mustBe testBody

        verify(
          1,
          postRequestedFor(urlEqualTo(URI(submitFaultToleranceUrl).getPath))
            .withRequestBody(equalToJson("""
                                           |{
                                           |  "etmpSafeId" : "etmpSafeId",
                                           |  "nominatedCompany" : {
                                           |    "name" : "String",
                                           |    "utr" : "String",
                                           |    "crn" : "String"
                                           |  },
                                           |  "contacts" : [ {
                                           |    "name" : "String",
                                           |    "email" : "String",
                                           |    "status" : "String",
                                           |    "language" : "String"
                                           |  } ],
                                           |  "idempotencyKey": "key"
                                           |}""".stripMargin))
        )
      }
    }
  }

  "A GET call from SignUpConnector.getStateOfWorkItem to the target URL" - {
    for status <- Seq(204, 400, 401, 500, 502) yield {
      s"must return the raw HttpResponse for status=$status" in {
        stubFor(
          get(urlEqualTo(getStateOfWorkUrl))
            .willReturn(
              aResponse()
                .withStatus(status)
            )
        )

        val result: HttpResponse =
          SUT
            .getStateOfWorkItem("key")
            .futureValue

        result.status mustBe status

        verify(
          1,
          getRequestedFor(urlEqualTo(URI(getStateOfWorkUrl).getPath))
        )
      }
    }
  }
}

object ProtectedServiceConnectorISpec {
  val testBody: String = "test response"
  val subscriptionId   = "123"
}
