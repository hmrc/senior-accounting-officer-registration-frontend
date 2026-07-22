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

package support

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames

object MockAuthHelper {

  val authSession: Map[String, String] = Map("authToken" -> "mock-bearer-token")

  val authoriseUri: String = "/auth/authorise"

  def mockAuthOk(): StubMapping =
    stubFor(
      post(urlEqualTo(authoriseUri))
        .willReturn(
          aResponse()
            .withHeader("content-type", "application/json")
            .withBody(s"""{
                | "internalId": "$testId"
                |}""".stripMargin)
            .withStatus(200)
        )
    )

  def mockAuthNoId(): StubMapping =
    stubFor(
      post(urlEqualTo(authoriseUri))
        .willReturn(
          aResponse()
            .withHeader("content-type", "application/json")
            .withBody("{}")
            .withStatus(200)
        )
    )

  def mockAuthEnroled(): StubMapping =
    stubFor(
      post(urlEqualTo(authoriseUri))
        .willReturn(
          aResponse()
            .withHeader("content-type", "application/json")
            .withBody(s"""{
                | "allEnrolments" : [{
                |   "key":"HMRC-DSAO-ORG",
                |   "identifiers": [{
                |     "key":"",
                |     "value": "$testSubscriptionId"
                |   }]
                | }]
                |}""".stripMargin)
            .withStatus(200)
        )
    )

  def mockAuthNoEnrolments(): StubMapping = {
    stubFor(
      post(urlEqualTo(authoriseUri))
        .willReturn(
          aResponse()
            .withHeader(HeaderNames.CONTENT_TYPE, "application/json")
            .withHeader(HeaderNames.AUTHORIZATION, testBearerToken)
            .withBody(s"""{
                   | "allEnrolments" : []
                   |}""".stripMargin)
            .withStatus(200)
        )
    )
  }

  def verifyAuthWasCalled(times: Int = 1): Unit =
    verify(times, postRequestedFor(urlEqualTo(authoriseUri)))

  val testBearerToken    = "mock-bearer-token"
  val testId             = "testId"
  val testSubscriptionId = "testSubscriptionId"

}
