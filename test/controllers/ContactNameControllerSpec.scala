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
import forms.ContactNameFormProvider
import models.{ContactType, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ContactNamePage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.ContactNameView

import scala.concurrent.Future

class ContactNameControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new ContactNameFormProvider()

  "ContactName Controller" - {
    ContactType.values.foreach { contactType =>
      s"When the ContactType is $contactType" - {
        lazy val form                 = formProvider(contactType)
        lazy val contactNameRoute     = routes.ContactNameController.onPageLoad(contactType, NormalMode).url
        lazy val contactNamePostRoute = routes.ContactNameController.onSubmit(contactType, NormalMode).url
        "must return OK and the correct view for a GET" in {
          val request     = FakeRequest(GET, contactNameRoute)
          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
          val view        = application.injector.instanceOf[ContactNameView]
          val controller  = application.injector.instanceOf[ContactNameController]
          running(application) {

            val result = controller.onPageLoad(contactType, NormalMode)(request)

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, contactType, NormalMode)(using
              request,
              messages(application)
            ).toString
          }
        }

        "must populate the view correctly on a GET when the question has previously been answered" in {
          val request     = FakeRequest(GET, contactNameRoute)
          val userAnswers = UserAnswers(userAnswersId).set(ContactNamePage(contactType), "answer").success.value
          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
          val view        = application.injector.instanceOf[ContactNameView]
          val controller  = application.injector.instanceOf[ContactNameController]
          running(application) {

            val result = controller.onPageLoad(contactType, NormalMode)(request)

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form.fill("answer"), contactType, NormalMode)(using
              request,
              messages(application)
            ).toString
          }
        }

        "must redirect to the next page when valid data is submitted" in {
          val request = FakeRequest(POST, contactNamePostRoute)
            .withFormUrlEncodedBody(("value", "answer"))
          val mockSessionRepository = mock[SessionRepository]
          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
          val application =
            applicationBuilder(userAnswers = Some(emptyUserAnswers))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()
          val controller = application.injector.instanceOf[ContactNameController]
          running(application) {

            val result = controller.onSubmit(contactType, NormalMode)(request)

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }

        "must return a Bad Request and errors when invalid data is submitted" in {
          val invalidInput = contactType match {
            case ContactType.First  => ""
            case ContactType.Second => ""
          }
          val boundForm = form.bind(Map("value" -> invalidInput))
          val request   = FakeRequest(POST, contactNamePostRoute)
            .withFormUrlEncodedBody(("value", invalidInput))
          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
          val view        = application.injector.instanceOf[ContactNameView]
          val controller  = application.injector.instanceOf[ContactNameController]
          running(application) {

            val result = controller.onSubmit(contactType, NormalMode)(request)

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(boundForm, contactType, NormalMode)(using
              request,
              messages(application)
            ).toString
          }
        }

        "must redirect to Journey Recovery for a GET if no existing data is found" in {
          val request     = FakeRequest(GET, contactNameRoute)
          val application = applicationBuilder(userAnswers = None).build()
          val controller  = application.injector.instanceOf[ContactNameController]
          running(application) {

            val result = controller.onPageLoad(contactType, NormalMode)(request)

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "must redirect to Journey Recovery for a POST if no existing data is found" in {
          val request     = FakeRequest(POST, contactNamePostRoute).withFormUrlEncodedBody(("value", "answer"))
          val application = applicationBuilder(userAnswers = None).build()
          val controller  = application.injector.instanceOf[ContactNameController]
          running(application) {

            val result = controller.onSubmit(contactType, NormalMode)(request)

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "must return a Bad Request for first contact when invalid characters are submitted" in {
          if contactType == ContactType.First then {
            val request = FakeRequest(POST, contactNamePostRoute)
              .withFormUrlEncodedBody(("value", "bad&name\""))
            val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
            val view        = application.injector.instanceOf[ContactNameView]
            val controller  = application.injector.instanceOf[ContactNameController]

            running(application) {
              val result = controller.onSubmit(contactType, NormalMode)(request)

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual view(
                form.bind(Map("value" -> "bad&name\"")),
                contactType,
                NormalMode
              )(using request, messages(application)).toString
            }
          }
        }
      }
    }
  }
}
