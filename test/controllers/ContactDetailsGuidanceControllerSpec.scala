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
import models.ContactType.First
import models.NormalMode
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.ContactDetailsGuidanceView

class ContactDetailsGuidanceControllerSpec extends SpecBase {

  "ContactDetailsGuidance Controller" - {
    "must redirect to index when contacts have been confirmed onPageLoad endpoint" in {
      val request     = FakeRequest(GET, routes.ContactDetailsGuidanceController.onPageLoad().url)
      val application = applicationBuilder(userAnswers = Some(userAnswersWithConfirmedContacts)).build()
      running(application) {

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustEqual Some(routes.IndexController.onPageLoad().url)
      }
    }
    "must redirect to index when contacts have been confirmed on continue endpoint" in {
      val request     = FakeRequest(GET, routes.ContactDetailsGuidanceController.continue().url)
      val application = applicationBuilder(userAnswers = Some(userAnswersWithConfirmedContacts)).build()
      running(application) {

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustEqual Some(routes.IndexController.onPageLoad().url)
      }
    }
    "must return OK and the correct view for a GET" in {
      val request     = FakeRequest(GET, routes.ContactDetailsGuidanceController.onPageLoad().url)
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val view        = application.injector.instanceOf[ContactDetailsGuidanceView]
      running(application) {

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(using request, messages(application)).toString
      }
    }

    "must Redirect to First Contact Name for a Continue" in {
      val request     = FakeRequest(POST, routes.ContactDetailsGuidanceController.onPageLoad().url)
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).get mustEqual routes.ContactNameController.onPageLoad(First, NormalMode).url
      }
    }
  }
}
