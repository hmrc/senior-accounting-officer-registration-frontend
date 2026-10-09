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

package controllers

import base.SpecBase
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status
import play.api.inject
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.SignUpService
import services.SignUpService.SignUpResult
import uk.gov.hmrc.http.InternalServerException

import scala.concurrent.Future

class RegistrationPendingControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  val mockSignUpService: SignUpService = mock[SignUpService]

  override def applicationBuilder(userAnswers: Option[UserAnswers] = None): GuiceApplicationBuilder =
    super
      .applicationBuilder()
      .overrides(
        inject.bind[SignUpService].toInstance(mockSignUpService)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSignUpService)
  }

  val errorScenarios: List[SignUpResult] =
    List(
      SignUpResult.InsufficientUserAnswers,
      SignUpResult.BadRequestFailure,
      SignUpResult.MalformedResponse,
      SignUpResult.ProtectedServiceFailure(Status.INTERNAL_SERVER_ERROR),
      SignUpResult.UnknownFailure(Status.IM_A_TEAPOT)
    )

  "RegistrationPendingController.onPageLoad" - {
    "must return OK and the correct view" - {
      "when signUpService returns a pending result" in {
        when(mockSignUpService.getRegistrationState(any())(using any()))
          .thenReturn(Future.successful(SignUpResult.Pending("key")))

        val application = applicationBuilder().build()

        running(application) {
          val request = FakeRequest(GET, routes.RegistrationPendingController.onPageLoad("key").url)
          val result  = route(application, request).value

          status(result) mustEqual Status.OK

          verify(mockSignUpService, times(1)).getRegistrationState(any())(using any())
        }
      }
    }

    "must return a redirect to the registration complete page" - {
      "when signUpService returns a success result" in {
        when(mockSignUpService.getRegistrationState(any())(using any()))
          .thenReturn(Future.successful(SignUpResult.Success("id")))

        val application = applicationBuilder().build()

        running(application) {
          val request = FakeRequest(GET, routes.RegistrationPendingController.onPageLoad("key").url)
          val result  = route(application, request).value

          status(result) mustEqual Status.SEE_OTHER
          redirectLocation(result).value mustEqual routes.RegistrationCompleteController.onPageLoad.url

          verify(mockSignUpService, times(1)).getRegistrationState(any())(using any())
        }
      }
    }

    "must throw an InternalServerException" - {
      errorScenarios.map { scenario =>
        s"when signUpService returns a $scenario" in {
          when(mockSignUpService.getRegistrationState(any())(using any()))
            .thenReturn(Future.successful(scenario))

          val application = applicationBuilder().build()

          running(application) {
            val request = FakeRequest(GET, routes.RegistrationPendingController.onPageLoad("key").url)
            val result  = route(application, request).value

            intercept[InternalServerException] {
              await(result)
            }

            verify(mockSignUpService, times(1)).getRegistrationState(any())(using any())
          }
        }
      }
    }
  }
}
