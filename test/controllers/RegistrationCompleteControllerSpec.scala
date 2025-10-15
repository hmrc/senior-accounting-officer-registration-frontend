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
import models.UserAnswers
import models.registration.CompanyDetails
import models.registration.RegistrationCompleteDetails
import pages.CompanyDetailsPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.RegistrationCompleteView

import java.time.{LocalDateTime, ZoneOffset, ZonedDateTime}

class RegistrationCompleteControllerSpec extends SpecBase {

  "RegistrationComplete Controller" - {
    lazy val registrationCompleteRoute = routes.RegistrationCompleteController.onPageLoad().url
    "must return OK and the correct view for a GET when company details are available" in {

      val userAnswersWithCompanyDetails =
        UserAnswers(id = "id")
          .set(
            CompanyDetailsPage,
            CompanyDetails(
              companyName = "ABC Ltd",
              companyNumber = "number",
              ctUtr = "ctUtr",
              registeredBusinessPartnerId = "registeredBusinessPartnerId"
            )
          )
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithCompanyDetails)).build()

      running(application) {
        val request          = FakeRequest(GET, registrationCompleteRoute)
        val result           = route(application, request).value
        val view             = application.injector.instanceOf[RegistrationCompleteView]
        val registrationData = RegistrationCompleteDetails(
          companyName = "ABC Ltd",
          registrationId = "XMPLR0123456789",
          registrationDateTime = ZonedDateTime.of(LocalDateTime.of(2025, 1, 17, 11, 45), ZoneOffset.UTC)
        )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(registrationData)(using request, messages(application)).toString
      }
    }

    "must redirect to the journey recovery view for a GET when company details are not available" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, registrationCompleteRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
