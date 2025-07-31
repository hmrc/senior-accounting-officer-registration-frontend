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
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.ContactCheckYourAnswersView
import models.{ContactInfo, UserAnswers}
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.guice.GuiceApplicationBuilder
import services.ContactCheckYourAnswersService
import play.api.inject.bind

class ContactCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar {

  override protected def applicationBuilder(userAnswers: Option[UserAnswers] = None): GuiceApplicationBuilder =
    super
      .applicationBuilder(userAnswers)
      .overrides(
        bind[ContactCheckYourAnswersService].toInstance(mock[ContactCheckYourAnswersService])
      )

  "ContactCheckYourAnswers Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val testContactInfos                   = List(ContactInfo("", "", "", ""))
        val mockContactCheckYourAnswersService = application.injector.instanceOf[ContactCheckYourAnswersService]
        when(mockContactCheckYourAnswersService.getContactInfos(any())).thenReturn(testContactInfos)
        val request = FakeRequest(GET, routes.ContactCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactCheckYourAnswersView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(testContactInfos)(
          request,
          messages(application)
        ).toString
      }
    }

     "must redirect to journey recovery when no contacts found" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val mockContactCheckYourAnswersService = application.injector.instanceOf[ContactCheckYourAnswersService]
        when(mockContactCheckYourAnswersService.getContactInfos(any())).thenReturn(List.empty)
        val request = FakeRequest(GET, routes.ContactCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustEqual Some(routes.JourneyRecoveryController.onPageLoad().url)
      }
    }
  }
}
