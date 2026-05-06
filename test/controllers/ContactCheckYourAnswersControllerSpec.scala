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
import models.ContactType.{First, Second}
import models.{ContactInfo, ContactType, UserAnswers}
import org.mockito.ArgumentMatchers.eq as meq
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.ContactCheckYourAnswersService
import views.html.ContactCheckYourAnswersView

class ContactCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar {
  val testUserAnswers: UserAnswers = emptyUserAnswers

  override protected def applicationBuilder(userAnswers: Option[UserAnswers] = None): GuiceApplicationBuilder =
    super
      .applicationBuilder(userAnswers)
      .overrides(
        bind[SessionRepository].toInstance(mock[SessionRepository]),
        bind[ContactCheckYourAnswersService].toInstance(mock[ContactCheckYourAnswersService])
      )

  "ContactCheckYourAnswers Controller" - {
    "when ContactType is First" - {

      "onPageLoad endpoint:" - {
        "must return OK and the correct view for a GET" in {
          val testContactInfo                    = ContactInfo("", "")
          val application                        = applicationBuilder(userAnswers = Some(testUserAnswers)).build()
          val view                               = application.injector.instanceOf[ContactCheckYourAnswersView]
          val mockContactCheckYourAnswersService = application.injector.instanceOf[ContactCheckYourAnswersService]
          when(mockContactCheckYourAnswersService.getContactInfo(meq(testUserAnswers), meq(First)))
            .thenReturn(Some(testContactInfo))

          running(application) {
            val request = FakeRequest(GET, routes.ContactCheckYourAnswersController.onPageLoad(First).url)
            val result  = route(application, request).value

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(testContactInfo, First)(using
              request,
              messages(application)
            ).toString
          }
        }

        "must redirect to journey recovery when no contacts found" in {
          val application                        = applicationBuilder(userAnswers = Some(testUserAnswers)).build()
          val mockContactCheckYourAnswersService = application.injector.instanceOf[ContactCheckYourAnswersService]
          when(mockContactCheckYourAnswersService.getContactInfo(meq(testUserAnswers), meq(First))).thenReturn(None)

          running(application) {
            val request = FakeRequest(GET, routes.ContactCheckYourAnswersController.onPageLoad(First).url)
            val result  = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustEqual Some(routes.JourneyRecoveryController.onPageLoad().url)
          }
        }
      }

      "saveAndContinue endpoint:" - {
//        "must redirect to contact have you added all controller when record saved" in {
//          val testContactInfo                    = List(ContactInfo("name", "email"))
//          val application                        = applicationBuilder(userAnswers = Some(testUserAnswers)).build()
//          val mockContactCheckYourAnswersService = application.injector.instanceOf[ContactCheckYourAnswersService]
//          when(mockContactCheckYourAnswersService.getContactInfo(meq(testUserAnswers), meq(First)))
//            .thenReturn(Some(testContactInfo))
//
//          running(application) {
//            val request = FakeRequest(POST, routes.ContactCheckYourAnswersController.saveAndContinue(First).url)
//            //Do I need to put a body??
////            .withBody(
////              "contacts[0].name"  -> "name",
////              "contacts[0].email" -> "email"
////            )
//            val result  = route(application, request).value
//
//            status(result) mustEqual SEE_OTHER
//            redirectLocation(result) mustEqual Some(routes.ContactHaveYouAddedAllController.onPageLoad(First).url)
//          }
//        }

        "must redirect to journey recovery when no contacts found" in {
          val application                        = applicationBuilder(userAnswers = Some(testUserAnswers)).build()
          val mockContactCheckYourAnswersService = application.injector.instanceOf[ContactCheckYourAnswersService]
          when(mockContactCheckYourAnswersService.getContactInfo(meq(testUserAnswers), meq(First))).thenReturn(None)

          running(application) {
            val request = FakeRequest(POST, routes.ContactCheckYourAnswersController.saveAndContinue(First).url)
            val result  = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustEqual Some(routes.JourneyRecoveryController.onPageLoad().url)
          }
        }
      }
    }

    "when ContactType is Second" - {
      "onPageLoad endpoint:" - {
        "must return OK and the correct view for a GET" in {
          val testContactInfo                    = ContactInfo("", "")
          val application                        = applicationBuilder(userAnswers = Some(testUserAnswers)).build()
          val view                               = application.injector.instanceOf[ContactCheckYourAnswersView]
          val mockContactCheckYourAnswersService = application.injector.instanceOf[ContactCheckYourAnswersService]
          when(mockContactCheckYourAnswersService.getContactInfo(meq(testUserAnswers), meq(Second)))
            .thenReturn(Some(testContactInfo))

          running(application) {
            val request = FakeRequest(GET, routes.ContactCheckYourAnswersController.onPageLoad(Second).url)
            val result  = route(application, request).value

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(testContactInfo, Second)(using
              request,
              messages(application)
            ).toString
          }
        }

        "must redirect to journey recovery when no contacts found" in {
          val application                        = applicationBuilder(userAnswers = Some(testUserAnswers)).build()
          val mockContactCheckYourAnswersService = application.injector.instanceOf[ContactCheckYourAnswersService]
          when(mockContactCheckYourAnswersService.getContactInfo(meq(testUserAnswers), meq(Second))).thenReturn(None)

          running(application) {
            val request = FakeRequest(GET, routes.ContactCheckYourAnswersController.onPageLoad(Second).url)
            val result  = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustEqual Some(routes.JourneyRecoveryController.onPageLoad().url)
          }
        }
      }

      "saveAndContinue endpoint:" - {
        "must redirect to index page when second record is saved" in {}

        "must redirect to journey recovery when no contacts found" in {
          val application                        = applicationBuilder(userAnswers = Some(testUserAnswers)).build()
          val mockContactCheckYourAnswersService = application.injector.instanceOf[ContactCheckYourAnswersService]
          when(mockContactCheckYourAnswersService.getContactInfo(meq(testUserAnswers), meq(Second))).thenReturn(None)

          running(application) {
            val request = FakeRequest(POST, routes.ContactCheckYourAnswersController.saveAndContinue(Second).url)
            val result  = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustEqual Some(routes.JourneyRecoveryController.onPageLoad().url)
          }
        }
      }
    }
  }
}
