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
import models.registration.CompanyDetails
import org.jsoup.Jsoup
import pages.CompanyDetailsPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.RegistrationCompleteView

class RegistrationCompleteControllerSpec extends SpecBase {

  "RegistrationComplete Controller" - {

    "must return OK and the correct view for a GET" in {
      val companyDetails = CompanyDetails(
        companyName = "Test Company",
        companyNumber = "123456",
        ctUtr = "1234567890",
        registeredBusinessPartnerId = "XB000000000001"
      )

      val userAnswers = emptyUserAnswers.set(CompanyDetailsPage, companyDetails).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, routes.RegistrationCompleteController.onPageLoad().url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[RegistrationCompleteView]

        status(result) mustEqual OK
        val content = contentAsString(result)
        val doc     = Jsoup.parse(content)

        doc.text() must include("Test Company")
        doc.text() must include("XB000000000001")
      }
    }

    "must redirect to journeyController if no userAnswers are found" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, routes.RegistrationCompleteController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
