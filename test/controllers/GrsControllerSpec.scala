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
import connectors.GrsConnector
import controllers.GrsControllerSpec.*
import models.UserAnswers
import models.grs.retrieve.Registration.Registered
import models.grs.retrieve.{CompanyProfile, CompanyDetails as GrsCompanyDetails}
import models.registration.CompanyDetails
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import pages.CompanyDetailsPage
import play.api.inject
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import uk.gov.hmrc.http.{HttpResponse, InternalServerException}
import utils.IdentifierGenerator

import java.util.UUID
import scala.concurrent.Future

class GrsControllerSpec extends SpecBase with MockitoSugar {

  override def applicationBuilder(userAnswers: Option[UserAnswers] = None): GuiceApplicationBuilder =
    super
      .applicationBuilder(userAnswers)
      .overrides(
        inject.bind[GrsConnector].toInstance(mock[GrsConnector])
      )

  "GrsController.start" - {
    "when GRS returns a valid response must redirect the user to the url in the GRS response" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val grsConnector = application.injector.instanceOf[GrsConnector]
        val request      = FakeRequest(GET, routes.GrsController.start().url)

        when(grsConnector.start(any())(using any())).thenReturn(
          Future.successful(HttpResponse(status = CREATED, body = validStartResponse))
        )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        headers(result).get("Location").value mustEqual testGrsJourneyStartLocation
      }
    }
    "when there is prior user answer must call GRS" - {
      "when GRS returns a valid response must redirect the user to the url in the GRS response" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val grsConnector = application.injector.instanceOf[GrsConnector]
          val request      = FakeRequest(GET, routes.GrsController.start().url)

          when(grsConnector.start(any())(using any())).thenReturn(
            Future.successful(HttpResponse(status = CREATED, body = validStartResponse))
          )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          headers(result).get("Location").value mustEqual testGrsJourneyStartLocation
        }
      }

      "when GRS returns a 201 with an invalid json must throw InternalServerException" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val grsConnector = application.injector.instanceOf[GrsConnector]
          val request      = FakeRequest(GET, routes.GrsController.start().url)

          when(grsConnector.start(any())(using any())).thenReturn(
            Future.successful(HttpResponse(status = CREATED))
          )

          val result = route(application, request).value

          intercept[InternalServerException](await(result))
        }
      }

      "when GRS returns a 404 must throw InternalServerException" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val grsConnector = application.injector.instanceOf[GrsConnector]
          val request      = FakeRequest(GET, routes.GrsController.start().url)

          when(grsConnector.start(any())(using any())).thenReturn(
            Future.successful(HttpResponse(status = NOT_FOUND))
          )

          val result = route(application, request).value

          intercept[InternalServerException](await(result))
        }
      }

      "when GRS returns a 500 must throw InternalServerException" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val grsConnector = application.injector.instanceOf[GrsConnector]
          val request      = FakeRequest(GET, routes.GrsController.start().url)

          when(grsConnector.start(any())(using any())).thenReturn(
            Future.successful(HttpResponse(status = INTERNAL_SERVER_ERROR))
          )

          val result = route(application, request).value

          intercept[InternalServerException](await(result))
        }
      }

    }
  }

  "GrsController.callBack" - {
    val journeyId = UUID.randomUUID().toString

    "when there are no user answers must store a new user answers with the sanitised company details in mongo then redirect to registration dashboard" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val grsConnector      = application.injector.instanceOf[GrsConnector]
        val request           = FakeRequest(GET, routes.GrsController.callBack(journeyId).url)
        val sessionRepository = application.injector.instanceOf[SessionRepository]

        when(grsConnector.retrieve(any())(using any())).thenReturn(
          Future.successful(Right(validGrsCompanyDetails))
        )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        headers(result).get("Location").value mustEqual "/senior-accounting-officer/registration"

        val inMongo = sessionRepository
          .get(userAnswersId)
          .futureValue
          .value
          .get(CompanyDetailsPage)
          .value

        inMongo mustBe CompanyDetails(
          companyName = testCompanyName,
          companyNumber = testCompanyNumber,
          ctUtr = testCtUtr,
          registeredBusinessPartnerId = testRegisteredBusinessPartnerId
        )
      }
    }

    "when there are prior user answer" - {
      "when GRS connector returns a valid company details must sanitise and store it in mongo and redirect to registration dashboard" in {
        val answer      = UserAnswers(id = UUID.randomUUID().toString)
        val application = applicationBuilder(userAnswers = Some(answer)).build()

        running(application) {
          val grsConnector      = application.injector.instanceOf[GrsConnector]
          val sessionRepository = application.injector.instanceOf[SessionRepository]

          when(grsConnector.retrieve(any())(using any())).thenReturn(
            Future.successful(Right(validGrsCompanyDetails))
          )

          val request = FakeRequest(GET, routes.GrsController.callBack(journeyId).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          headers(result).get("Location").value mustEqual "/senior-accounting-officer/registration"

          val inMongo = sessionRepository
            .get(answer.id)
            .futureValue
            .value
            .get(CompanyDetailsPage)
            .value

          inMongo mustBe CompanyDetails(
            companyName = testCompanyName,
            companyNumber = testCompanyNumber,
            ctUtr = testCtUtr,
            registeredBusinessPartnerId = testRegisteredBusinessPartnerId
          )
        }
      }

      "when GRS connector returns Left(None) response must throw InternalServerException" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val grsConnector = application.injector.instanceOf[GrsConnector]

          when(grsConnector.retrieve(any())(using any())).thenReturn(
            Future.successful(Left(None))
          )

          val request = FakeRequest(GET, routes.GrsController.callBack(journeyId).url)

          val result = route(application, request).value

          intercept[InternalServerException](await(result))
        }
      }

      "when GRS connector returns Left(Exception) response must throw InternalServerException" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val grsConnector = application.injector.instanceOf[GrsConnector]

          when(grsConnector.retrieve(any())(using any())).thenReturn(
            Future.successful(Left(new Exception("")))
          )

          val request = FakeRequest(GET, routes.GrsController.callBack(journeyId).url)

          val result = route(application, request).value

          intercept[InternalServerException](await(result))
        }
      }

    }
  }
}

object GrsControllerSpec {
  val testGrsJourneyStartLocation = "test-location"
  val validStartResponse: String  =
    s"""
      | { "journeyStartUrl" : "$testGrsJourneyStartLocation"}
      |""".stripMargin

  val testCompanyName: String                 = "Test Company Ltd 2"
  val testCompanyNumber: String               = IdentifierGenerator.randomCompanyNumber
  val testRegisteredBusinessPartnerId: String = IdentifierGenerator.randomSafeId
  val testCtUtr: String                       = IdentifierGenerator.randomUtr

  val validGrsCompanyDetails: GrsCompanyDetails = GrsCompanyDetails(
    companyProfile = CompanyProfile(companyName = testCompanyName, companyNumber = testCompanyNumber),
    ctutr = testCtUtr,
    identifiersMatch = true,
    businessVerification = None,
    registration = Registered(registeredBusinessPartnerId = testRegisteredBusinessPartnerId)
  )

}
