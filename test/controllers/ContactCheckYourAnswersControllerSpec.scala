/*
 * Copyright 2026 HM Revenue & Customs
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
import models.*
import models.ContactType.First
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.eq as meq
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.ContactCheckYourAnswersService
import views.html.{ContactCheckYourAnswersView, ContactsCheckYourAnswersView}

class ContactCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar {
  def onwardRoute: Call                      = Call("GET", "/foo")
  val testUserAnswers: UserAnswers           = emptyUserAnswers
  val testContacts: ContactsCheckYourAnswers = ContactsCheckYourAnswers(
    firstContact = ContactInfo("name1", "email1"),
    secondContact = Some(ContactInfo("name2", "email2")),
    contactHaveYouAddedAll = ContactHaveYouAddedAll.No
  )

  override protected def applicationBuilder(userAnswers: Option[UserAnswers] = None): GuiceApplicationBuilder =
    super
      .applicationBuilder(userAnswers)
      .overrides(
        bind[SessionRepository].toInstance(mock[SessionRepository]),
        bind[ContactCheckYourAnswersService].toInstance(mock[ContactCheckYourAnswersService])
      )

  "ContactCheckYourAnswers Controller" - {
    "legacy flow when feature switch is off" - {
      "onPageLoad endpoint:" - {
        "must return OK and the correct view for a GET" in {
          val testContactInfo                    = ContactInfo("name", "email")
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
          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request = FakeRequest(GET, routes.ContactCheckYourAnswersController.onPageLoad(First).url)
            val result  = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustEqual Some(routes.JourneyRecoveryController.onPageLoad().url)
          }
        }
      }

      "saveAndContinue endpoint:" - {
        "must redirect to the next page for a POST" in {
          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
            .build()

          running(application) {
            val request = FakeRequest(POST, routes.ContactCheckYourAnswersController.saveAndContinue(First).url)
            val result  = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }
      }
    }

    "new flow when feature switch is on" - {
      "onPageLoadNew endpoint:" - {
        "must return OK and the correct combined view for a GET" in {
          val application = applicationBuilder(userAnswers = Some(testUserAnswers))
            .configure("features.contactFlowReshuffle" -> true)
            .build()
          val view                               = application.injector.instanceOf[ContactsCheckYourAnswersView]
          val mockContactCheckYourAnswersService = application.injector.instanceOf[ContactCheckYourAnswersService]
          when(mockContactCheckYourAnswersService.getContactsForCheckYourAnswers(meq(testUserAnswers)))
            .thenReturn(Some(testContacts))

          running(application) {
            val request = FakeRequest(GET, routes.ContactCheckYourAnswersController.onPageLoadNew().url)
            val result  = route(application, request).value

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(testContacts)(using request, messages(application)).toString
          }
        }

        "must redirect to journey recovery when feature switch is off" in {
          val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, routes.ContactCheckYourAnswersController.onPageLoadNew().url)
            val result  = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustEqual Some(routes.JourneyRecoveryController.onPageLoad().url)
          }
        }
      }

      "saveAndContinueNew endpoint:" - {
        "must redirect to the next page for a POST" in {
          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .configure("features.contactFlowReshuffle" -> true)
            .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
            .build()

          running(application) {
            val request = FakeRequest(POST, routes.ContactCheckYourAnswersController.saveAndContinueNew().url)
            val result  = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }
      }
    }
  }
}
