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
import forms.ContactHaveYouAddedAllFormProvider
import models.{ContactHaveYouAddedAll, ContactType, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ContactHaveYouAddedAllPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.ContactHaveYouAddedAllView

import scala.concurrent.Future

class ContactHaveYouAddedAllControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new ContactHaveYouAddedAllFormProvider()
  val form         = formProvider()

  "ContactHaveYouAddedAll Controller" - {
    List(ContactType.First, ContactType.Second).foreach { contactType =>
      s"When the ContactType is $contactType" - {

        lazy val contactHaveYouAddedAllRoute = routes.ContactHaveYouAddedAllController.onPageLoad(contactType).url

        "must return OK and the correct view for a GET" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, contactHaveYouAddedAllRoute)

            val result = route(application, request).value

            val view = application.injector.instanceOf[ContactHaveYouAddedAllView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, contactType)(request, messages(application)).toString
          }
        }

        "must populate the view correctly on a GET when the question has previously been answered" in {

          val userAnswers =
            UserAnswers(userAnswersId)
              .set(ContactHaveYouAddedAllPage(contactType), ContactHaveYouAddedAll.values.head)
              .success
              .value

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, contactHaveYouAddedAllRoute)

            val view = application.injector.instanceOf[ContactHaveYouAddedAllView]

            val result = route(application, request).value

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form.fill(ContactHaveYouAddedAll.values.head), contactType)(
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
              FakeRequest(POST, contactHaveYouAddedAllRoute)
                .withFormUrlEncodedBody(("value", ContactHaveYouAddedAll.values.head.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }

        "must return a Bad Request and errors when invalid data is submitted" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

          running(application) {
            val request =
              FakeRequest(POST, contactHaveYouAddedAllRoute)
                .withFormUrlEncodedBody(("value", "invalid value"))

            val boundForm = form.bind(Map("value" -> "invalid value"))

            val view = application.injector.instanceOf[ContactHaveYouAddedAllView]

            val result = route(application, request).value

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(boundForm, contactType)(request, messages(application)).toString
          }
        }

        "must redirect to Journey Recovery for a GET if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request = FakeRequest(GET, contactHaveYouAddedAllRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "redirect to Journey Recovery for a POST if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request =
              FakeRequest(POST, contactHaveYouAddedAllRoute)
                .withFormUrlEncodedBody(("value", ContactHaveYouAddedAll.values.head.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          }
        }
      }
    }
  }
}
