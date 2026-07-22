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
import models.{DashboardStage, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq as meq}
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.SignUpService.SignUpResult
import services.{DashboardService, SignUpService}
import uk.gov.hmrc.http.InternalServerException
import views.html.DashboardView

import scala.concurrent.Future

class IndexControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  val mockDashboardService: DashboardService   = mock[DashboardService]
  val mockSignUpService: SignUpService         = mock[SignUpService]
  val mockSessionRepository: SessionRepository = mock[SessionRepository]

  override def applicationBuilder(userAnswers: Option[UserAnswers] = None): GuiceApplicationBuilder =
    super
      .applicationBuilder(userAnswers)
      .overrides(
        bind[DashboardService].toInstance(mockDashboardService),
        bind[SignUpService].toInstance(mockSignUpService),
        bind[SessionRepository].toInstance(mockSessionRepository)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDashboardService)
    reset(mockSignUpService)
    reset(mockSessionRepository)
  }

  "IndexController.onPageLoad" - {

    "must return OK and the correct view" in {
      val testUserAnswer     = Some(emptyUserAnswers)
      val testDashboardStage = DashboardStage.Submission
      when(mockDashboardService.deriveCurrentStage(meq(testUserAnswer))).thenReturn(testDashboardStage)

      val application = applicationBuilder(userAnswers = testUserAnswer).build()

      running(application) {
        val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DashboardView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(testDashboardStage)(using
          request,
          messages(application)
        ).toString
      }
    }

  }

  "IndexController.submit" - {
    "when SignUpService returns Success" - {
      "must wipe Mongo and continue to Registration Complete page" - {
        "when Mongo is successfully cleared" in {
          when(mockSignUpService.submit(any())(using any()))
            .thenReturn(Future.successful(SignUpResult.Success("subscriptionId")))
          val testAnswers = emptyUserAnswers
          when(mockSessionRepository.clear(any())).thenReturn(Future.successful(true))

          val application = applicationBuilder(userAnswers = Some(testAnswers)).build()

          running(application) {
            val request = FakeRequest(POST, routes.IndexController.submit().url)
            val result  = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.RegistrationCompleteController.onPageLoad.url

            verify(mockSignUpService, times(1)).submit(meq(testAnswers))(using any())
            verify(mockSessionRepository, times(1)).clear(meq(userAnswersId))
          }
        }

        "when clearing Mongo return an error" in {
          when(mockSignUpService.submit(any())(using any()))
            .thenReturn(Future.successful(SignUpResult.Success("subscriptionId")))
          val testAnswers = emptyUserAnswers
          when(mockSessionRepository.clear(any())).thenReturn(Future.failed(new RuntimeException("test exception")))

          val application = applicationBuilder(userAnswers = Some(testAnswers)).build()

          running(application) {
            val request = FakeRequest(POST, routes.IndexController.submit().url)
            val result  = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.RegistrationCompleteController.onPageLoad.url

            verify(mockSignUpService, times(1)).submit(meq(testAnswers))(using any())
            verify(mockSessionRepository, times(1)).clear(meq(userAnswersId))
          }
        }
      }
    }

    for (key, value) <- Map(
        "InsufficientUserAnswers" -> SignUpResult.InsufficientUserAnswers,
        "MalformedResponse"       -> SignUpResult.MalformedResponse,
        "BadRequestFailure"       -> SignUpResult.BadRequestFailure,
        "ProtectedServiceFailure" -> SignUpResult.ProtectedServiceFailure(500),
        "UnknownFailure"          -> SignUpResult.UnknownFailure(500)
      )
    do {
      s"when SignUpService returns $key" - {
        "must throw an InternalServerException" in {
          when(mockSignUpService.submit(any())(using any()))
            .thenReturn(Future.successful(value))
          val testAnswers = emptyUserAnswers

          val application = applicationBuilder(userAnswers = Some(testAnswers)).build()

          running(application) {
            val request = FakeRequest(POST, routes.IndexController.submit().url)
            val result  = route(application, request).value

            intercept[InternalServerException] {
              await(result)
            }

            verify(mockSignUpService, times(1)).submit(meq(testAnswers))(using any())
            verify(mockSessionRepository, times(0)).clear(any())
          }
        }
      }
    }
  }
}
