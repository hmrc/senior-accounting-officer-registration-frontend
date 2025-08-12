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
import forms.ContactRoleFormProvider
import models.{ContactType, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ContactRolePage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.ContactRoleView

import scala.concurrent.Future

class ContactRoleControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new ContactRoleFormProvider()
  val form         = formProvider()

  "ContactRole Controller" - {
    ContactType.values.foreach { contactType =>
      s"When the ContactType is $contactType" - {
        lazy val contactRoleRoute = routes.ContactRoleController.onPageLoad(contactType, NormalMode).url
        s"must redirect to index when contacts have been submitted onPageLoad endpoint with contactType: $contactType" in {
          val application = applicationBuilder(userAnswers = Some(userAnswersWithContacts)).build()
          running(application) {
            val request = FakeRequest(GET, contactRoleRoute)
            val result  = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustEqual Some(routes.IndexController.onPageLoad().url)
          }
        }
        "must return OK and the correct view for a GET" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, contactRoleRoute)

            val result = route(application, request).value

            val view = application.injector.instanceOf[ContactRoleView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, contactType, NormalMode)(
              request,
              messages(application)
            ).toString
          }
        }

        "must populate the view correctly on a GET when the question has previously been answered" in {

          val userAnswers = UserAnswers(userAnswersId).set(ContactRolePage(contactType), "answer").success.value

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, contactRoleRoute)

            val view = application.injector.instanceOf[ContactRoleView]

            val result = route(application, request).value

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form.fill("answer"), contactType, NormalMode)(
              request,
              messages(application)
            ).toString
          }
        }

        "must redirect to the next page when valid data is submitted" in {

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val application =
            applicationBuilder(userAnswers = Some(emptyUserAnswers))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, contactRoleRoute)
                .withFormUrlEncodedBody(("value", "answer"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }

        "must return a Bad Request and errors when invalid data is submitted" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

          running(application) {
            val request =
              FakeRequest(POST, contactRoleRoute)
                .withFormUrlEncodedBody(("value", ""))

            val boundForm = form.bind(Map("value" -> ""))

            val view = application.injector.instanceOf[ContactRoleView]

            val result = route(application, request).value

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(boundForm, contactType, NormalMode)(
              request,
              messages(application)
            ).toString
          }
        }

        "must redirect to Journey Recovery for a GET if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request = FakeRequest(GET, contactRoleRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          }
        }
        s"must redirect to index when contacts have been submitted onSubmit endpoint with contactType: $contactType" in {
          val application = applicationBuilder(userAnswers = Some(userAnswersWithContacts)).build()
          running(application) {
            val request = FakeRequest(POST, routes.ContactRoleController.onSubmit(contactType, NormalMode).url)
            val result  = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustEqual Some(routes.IndexController.onPageLoad().url)
          }
        }
        "must redirect to Journey Recovery for a POST if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request =
              FakeRequest(POST, contactRoleRoute)
                .withFormUrlEncodedBody(("value", "answer"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          }
        }
      }
    }
  }
}
