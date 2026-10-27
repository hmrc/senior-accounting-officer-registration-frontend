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
import config.FeatureToggleSupport
import models.ContactType.First
import models.{config, *}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.{any, eq as meq}
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.ContactCheckYourAnswersService
import services.ContactUserAnswersService
import views.html.ContactsCheckYourAnswersView

class ContactCheckYourAnswersControllerSpec
    extends SpecBase
    with MockitoSugar
    with FeatureToggleSupport
    with BeforeAndAfterEach {
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
        bind[ContactCheckYourAnswersService].toInstance(mock[ContactCheckYourAnswersService]),
        bind[ContactUserAnswersService].toInstance(mock[ContactUserAnswersService])
      )

  "ContactCheckYourAnswers Controller" - {
    "onPageLoad endpoint:" - {
      "must return OK and the correct combined view for a GET" in {
        val application = applicationBuilder(userAnswers = Some(testUserAnswers))
          .build()
        val view = application.injector.instanceOf[ContactsCheckYourAnswersView]

        val mockContactCheckYourAnswersService = application.injector.instanceOf[ContactCheckYourAnswersService]
        when(mockContactCheckYourAnswersService.getContactsForCheckYourAnswers(meq(testUserAnswers)))
          .thenReturn(Some(testContacts))

        val mockUserAnswersService = application.injector.instanceOf[ContactUserAnswersService]
        when(mockUserAnswersService.sanitise(meq(testUserAnswers)))
          .thenReturn(testUserAnswers)

        running(application) {
          val request = FakeRequest(GET, routes.ContactCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(testContacts)(using request, messages(application)).toString
        }

        verify(mockUserAnswersService, times(1)).sanitise(meq(testUserAnswers))
      }

      "must redirect to journey recovery when check your answers service returns None" in {
        val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()

        val mockCheckYourAnswersService = application.injector.instanceOf[ContactCheckYourAnswersService]

        when(mockCheckYourAnswersService.getContactsForCheckYourAnswers(any())).thenReturn(None)

        val mockUserAnswersService = application.injector.instanceOf[ContactUserAnswersService]
        when(mockUserAnswersService.sanitise(meq(testUserAnswers)))
          .thenReturn(testUserAnswers)

        running(application) {
          val request = FakeRequest(GET, routes.ContactCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustEqual Some(routes.JourneyRecoveryController.onPageLoad().url)
        }

        verify(mockUserAnswersService, times(1)).sanitise(meq(testUserAnswers))
      }
    }

    "saveAndContinue endpoint:" - {
      "must redirect to the next page for a POST" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
          .build()

        running(application) {
          val request = FakeRequest(POST, routes.ContactCheckYourAnswersController.saveAndContinue().url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }
    }

  }
}
