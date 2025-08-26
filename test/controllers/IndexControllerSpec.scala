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
import views.html.DashboardView
import pages.CompanyDetailsPage
import models.registration.CompanyDetails
import pages.ContactsPage
import models.ContactInfo
import pages.SubmitAnswersPage
import org.apache.pekko.http.scaladsl.model.HttpCharsetRange.*

class IndexControllerSpec extends SpecBase {

  "IndexController Controller" - {
    "onPageLoad" - {
      "must return OK and the correct view for a GET" - {
        "given no user answers, must return companyDetails state" in {
          val request     = FakeRequest(GET, routes.IndexController.onPageLoad().url)
          val application = applicationBuilder(userAnswers = None).build()
          running(application) {
            val view = application.injector.instanceOf[DashboardView]

            val result = route(application, request).value

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(DashboardState.CompanyDetails)(
              request,
              messages(application)
            ).toString
          }
        }
        "given companyDetails is not empty, must return contactDetails state" in {
          val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)
          val answers = emptyUserAnswers
            .set(CompanyDetailsPage, CompanyDetails("name", "number", "ctUtr", "businessPartnerId"))
            .success
            .value
          val application = applicationBuilder(userAnswers = Some(answers)).build()
          running(application) {
            val view = application.injector.instanceOf[DashboardView]

            val result = route(application, request).value

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(DashboardState.ContactDetails)(
              request,
              messages(application)
            ).toString
          }
        }
        "given contactDetails is not empty, must return submitAnswers state" in {
          val request        = FakeRequest(GET, routes.IndexController.onPageLoad().url)
          val companyAnswers = emptyUserAnswers
            .set(CompanyDetailsPage, CompanyDetails("name", "number", "ctUtr", "businessPartnerId"))
            .success
            .value
          val contactAnswers = companyAnswers
            .set(ContactsPage, List(ContactInfo("name", "email", "phone", "address")))
            .success
            .value
          val application = applicationBuilder(userAnswers = Some(contactAnswers)).build()
          running(application) {
            val view = application.injector.instanceOf[DashboardView]

            val result = route(application, request).value

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(DashboardState.ContactDetails)(
              request,
              messages(application)
            ).toString
          }
        }
        "given submitAnswersPage is not empty, must return AnswersSubmitted state" in {
          val request        = FakeRequest(GET, routes.IndexController.onPageLoad().url)
          val companyAnswers = emptyUserAnswers
            .set(CompanyDetailsPage, CompanyDetails("name", "number", "ctUtr", "businessPartnerId"))
            .success
            .value
          val contactAnswers = companyAnswers
            .set(ContactsPage, List(ContactInfo("name", "email", "phone", "address")))
            .success
            .value
          val submitAnswers = contactAnswers.set(SubmitAnswersPage, true).success.value
          val application   = applicationBuilder(userAnswers = Some(submitAnswers)).build()
          running(application) {
            val view = application.injector.instanceOf[DashboardView]

            val result = route(application, request).value

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(DashboardState.ContactDetails)(
              request,
              messages(application)
            ).toString
          }
        }
      }
    }
  }
}
