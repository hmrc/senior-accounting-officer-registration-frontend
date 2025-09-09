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
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.config.FeatureToggle.StubGrs
import config.FeatureToggleSupport
import connectors.GrsConnectorISpec.*
import models.grs.create.{NewJourneyRequest, ServiceLabels}
import models.grs.retrieve.*
import models.grs.retrieve.Registration.Registered
import org.scalatest.EitherValues
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.Request
import play.api.test.FakeRequest
import support.ISpecBase
import utils.IdentifierGenerator

import java.util.UUID

class GrsConnectorISpec extends ISpecBase with FeatureToggleSupport with EitherValues {

  override def additionalConfigs: Map[String, Any] = Map(
    "features.stubGrs"                                                             -> "true",
    "microservice.services.incorporated-entity-identification-frontend-stubs.port" -> wireMockPort
  )

  val SUT          = app.injector.instanceOf[GrsConnector]
  given Request[?] = FakeRequest()

  "GrsConnector.start must hit the correct endpoint" - {
    val testInput = NewJourneyRequest(
      continueUrl = "continueUrl",
      deskProServiceId = "deskProServiceId",
      signOutUrl = "signOutUrl",
      regime = "regime",
      accessibilityUrl = "accessibilityUrl",
      businessVerificationCheck = false,
      labels = ServiceLabels(en = "Service Name")
    )
    "when features.stubGrs = true" in {
      enable(StubGrs)
      mockGrsStart()

      val response = SUT.start(testInput).futureValue

      response.status mustBe Status.OK
      verify(
        1,
        postRequestedFor(urlEqualTo("/grs-stub/start"))
          .withRequestBody(equalTo(Json.toJson(testInput).toString))
      )
    }

    "when features.stubGrs = false" in {
      disable(StubGrs)
      mockGrsStart()

      val response = SUT.start(testInput).futureValue

      response.status mustBe Status.OK
      verify(
        1,
        postRequestedFor(urlEqualTo("/incorporated-entity-identification/api/limited-company-journey"))
          .withRequestBody(equalTo(Json.toJson(testInput).toString))
      )
    }
  }

  "GrsConnector.retrieve" - {
    val testJourneyId = UUID.randomUUID().toString
    "must hit the correct endpoint" - {
      "when features.stubGrs = true" in {
        enable(StubGrs)
        mockGrsRetrieve()

        SUT.retrieve(testJourneyId).futureValue

        verify(
          1,
          getRequestedFor(urlEqualTo(s"/grs-stub/journey/$testJourneyId"))
        )
      }

      "when features.stubGrs = false" in {
        disable(StubGrs)
        mockGrsRetrieve()

        SUT.retrieve(testJourneyId).futureValue

        verify(
          1,
          getRequestedFor(urlEqualTo(s"/incorporated-entity-identification/api/journey/$testJourneyId"))
        )
      }
    }
    "must return Right(CompanyDetails) for a valid response" in {
      disable(StubGrs)
      mockGrsRetrieve()

      val response = SUT.retrieve(testJourneyId).futureValue

      response mustBe Right(
        CompanyDetails(
          CompanyProfile(testCompanyName, testCompanyNumber),
          testCtUtr,
          true,
          None,
          Registered(testRegisteredBusinessPartnerId)
        )
      )

      verify(
        1,
        getRequestedFor(urlEqualTo(s"/incorporated-entity-identification/api/journey/$testJourneyId"))
      )
    }

    "must return Left(None) for a 404 response" in {
      disable(StubGrs)
      mockGrsRetrieve(status = Status.NOT_FOUND)

      val response = SUT.retrieve(testJourneyId).futureValue

      response mustBe Left(None)

      verify(
        1,
        getRequestedFor(urlEqualTo(s"/incorporated-entity-identification/api/journey/$testJourneyId"))
      )
    }

    "must return Left(Some(Exception)) for a 500 response" in {
      disable(StubGrs)
      mockGrsRetrieve(status = Status.INTERNAL_SERVER_ERROR)

      val response = SUT.retrieve(testJourneyId).futureValue

      response mustBe a[Left[?, ?]]
      response.left.value mustBe a[Some[?]]
      response.left.value.get mustBe a[Exception]

      verify(
        1,
        getRequestedFor(urlEqualTo(s"/incorporated-entity-identification/api/journey/$testJourneyId"))
      )
    }

    "must return Left(Some(Exception)) for a 200 response with invalid payload" in {
      disable(StubGrs)
      mockGrsRetrieve(body = "")

      val response = SUT.retrieve(testJourneyId).futureValue

      response mustBe a[Left[?, ?]]
      response.left.value mustBe a[Some[?]]
      response.left.value.get mustBe a[Exception]

      verify(
        1,
        getRequestedFor(urlEqualTo(s"/incorporated-entity-identification/api/journey/$testJourneyId"))
      )
    }
  }

}

object GrsConnectorISpec {

  def mockGrsStart(): StubMapping =
    stubFor(
      post(anyUrl())
        .willReturn(
          aResponse()
            .withHeader("content-type", "application/json")
            .withBody("""{
              | "journeyStartUrl": "testUrl"
              |}""".stripMargin)
            .withStatus(200)
        )
    )

  def mockGrsRetrieve(status: Int = 200, body: String = validGrsRetrieveJson): StubMapping =
    stubFor(
      get(anyUrl())
        .willReturn(
          aResponse()
            .withHeader("content-type", "application/json")
            .withBody(body)
            .withStatus(status)
        )
    )

  val testCompanyName: String                 = "Test Company Ltd"
  val testCompanyNumber: String               = IdentifierGenerator.randomCompanyNumber
  val testRegisteredBusinessPartnerId: String = IdentifierGenerator.randomSafeId
  val testCtUtr: String                       = IdentifierGenerator.randomUtr

  val validGrsRetrieveJson: String =
    s"""
      |{"companyProfile":{"companyName":"$testCompanyName","companyNumber":"$testCompanyNumber","dateOfIncorporation":"2020-01-01","unsanitisedCHROAddress":{"address_line_1":"testLine1","address_line_2":"test town","care_of":"test name","country":"United Kingdom","locality":"test city","po_box":"123","postal_code":"AA11AA","premises":"1","region":"test region"}},"identifiersMatch":true,"registration":{"registrationStatus":"REGISTERED","registeredBusinessPartnerId":"$testRegisteredBusinessPartnerId"},"ctutr":"$testCtUtr"}
      |""".stripMargin
}
