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
import org.mockito.ArgumentMatchers.eq as meq
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.DashboardService
import views.html.DashboardView

class IndexControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  val mockDashboardService: DashboardService = mock[DashboardService]

  override def applicationBuilder(userAnswers: Option[UserAnswers] = None): GuiceApplicationBuilder =
    super
      .applicationBuilder(userAnswers)
      .overrides(bind[DashboardService].toInstance(mockDashboardService))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDashboardService)
  }

  "IndexController Controller" - {

    "must return OK and the correct view for a GET" in {
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

    "must continue to Registration Complete page for POST" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, routes.IndexController.continue().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.RegistrationCompleteController.onPageLoad().url
      }
    }
  }
}
