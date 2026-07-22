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

  def testUrl = "/senior-accounting-officer-registration/sign-up"

  "A POST call from SignUpConnector.submit to the target URL" - {
    for status <- Seq(200, 400, 401, 500, 502) yield {
      s"must return the raw HttpResponse for status=$status" in {
        stubFor(
          post(urlEqualTo(testUrl))
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
                )
              )
            )
            .futureValue

        result.status mustBe status
        result.body mustBe testBody

        verify(
          1,
          postRequestedFor(urlEqualTo(URI(testUrl).getPath))
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
}

object ProtectedServiceConnectorISpec {
  val testBody: String = "test response"
  val subscriptionId   = "123"
}
