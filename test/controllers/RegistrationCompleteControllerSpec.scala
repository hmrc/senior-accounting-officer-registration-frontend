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
import models.registration.RegistrationCompleteDetails
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.RegistrationCompleteView

import java.time.LocalDateTime

class RegistrationCompleteControllerSpec extends SpecBase {

  "RegistrationComplete Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request          = FakeRequest(GET, routes.RegistrationCompleteController.onPageLoad().url)
        val result           = route(application, request).value
        val view             = application.injector.instanceOf[RegistrationCompleteView]
        val registrationData = RegistrationCompleteDetails(
          companyName = "ABC Ltd",
          registrationId = "XMPLR0123456789",
          registrationDateTime = LocalDateTime.of(2025, 1, 17, 11, 45, 0)
        )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(registrationData)(request, messages(application)).toString
      }
    }
  }
}
