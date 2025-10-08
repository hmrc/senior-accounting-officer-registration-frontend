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
import models.{ContactInfo, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq as meq}
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import pages.ContactsPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.ContactCheckYourAnswersService
import uk.gov.hmrc.http.BadRequestException
import views.html.ContactCheckYourAnswersView

import scala.concurrent.Future

class ContactCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar {
  val testUserAnswers = emptyUserAnswers

  override protected def applicationBuilder(userAnswers: Option[UserAnswers] = None): GuiceApplicationBuilder =
    super
      .applicationBuilder(userAnswers)
      .overrides(
        bind[SessionRepository].toInstance(mock[SessionRepository]),
        bind[ContactCheckYourAnswersService].toInstance(mock[ContactCheckYourAnswersService])
      )

  "ContactCheckYourAnswers Controller" - {
    "onPageLoad endpoint:" - {
      "must return OK and the correct view for a GET" in {
        val request          = FakeRequest(GET, routes.ContactCheckYourAnswersController.onPageLoad().url)
        val testContactInfos = List(ContactInfo("", ""))
        val application      = applicationBuilder(userAnswers = Some(testUserAnswers)).build()
        val view             = application.injector.instanceOf[ContactCheckYourAnswersView]
        val mockContactCheckYourAnswersService = application.injector.instanceOf[ContactCheckYourAnswersService]
        when(mockContactCheckYourAnswersService.getContactInfos(meq(testUserAnswers))).thenReturn(testContactInfos)
        running(application) {

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(testContactInfos)(using
            request,
            messages(application)
          ).toString
        }
      }

      "must redirect to journey recovery when no contacts found" in {
        val request     = FakeRequest(GET, routes.ContactCheckYourAnswersController.onPageLoad().url)
        val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()
        val mockContactCheckYourAnswersService = application.injector.instanceOf[ContactCheckYourAnswersService]
        when(mockContactCheckYourAnswersService.getContactInfos(meq(testUserAnswers))).thenReturn(List.empty)
        running(application) {

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustEqual Some(routes.JourneyRecoveryController.onPageLoad().url)
        }
      }

      "must redirect to index when contacts have been confirmed" in {
        val request     = FakeRequest(GET, routes.ContactCheckYourAnswersController.onPageLoad().url)
        val application = applicationBuilder(userAnswers = Some(userAnswersWithConfirmedContacts)).build()
        running(application) {

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustEqual Some(routes.IndexController.onPageLoad().url)
        }
      }
    }
  }

  "saveAndContinue endpoint:" - {
    "must redirect to index controller when record saved" - {
      "Data saved to the SessionRepository must be the sanitised contacts" in {
        val request = FakeRequest(POST, routes.ContactCheckYourAnswersController.saveAndContinue().url)
          .withFormUrlEncodedBody(
            "contacts[0].name"  -> "name",
            "contacts[0].email" -> "email",
          )
        val testContactInfos = List(ContactInfo("name", "email"))
        val application      = applicationBuilder(userAnswers = Some(testUserAnswers)).build()
        running(application) {
          val mockedSessionRepository = application.injector.instanceOf[SessionRepository]
          when(mockedSessionRepository.set(any())).thenReturn(Future.successful(true))
          val mockContactCheckYourAnswersService = application.injector.instanceOf[ContactCheckYourAnswersService]
          when(mockContactCheckYourAnswersService.getContactInfos(meq(testUserAnswers))).thenReturn(testContactInfos)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustEqual Some(routes.IndexController.onPageLoad().url)
          withClue("verify that the mongo instance was called once to update with bound Page -> UserAnswers\n") {
            verify(mockedSessionRepository, times(1)).set(testUserAnswers.set(ContactsPage, testContactInfos).get)
          }
        }
      }
    }

    "must throw BadRequestException when service contactInfo and form are misaligned" in {
      val testContactInfos = List(ContactInfo("name", "email"))
      val request          = FakeRequest(POST, routes.ContactCheckYourAnswersController.saveAndContinue().url)
        .withFormUrlEncodedBody(
          "contacts[0].name"  -> "differentName",
          "contacts[0].email" -> "stolenEmail",
        )
      val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()
      running(application) {
        val mockContactCheckYourAnswersService = application.injector.instanceOf[ContactCheckYourAnswersService]
        when(mockContactCheckYourAnswersService.getContactInfos(meq(testUserAnswers))).thenReturn(testContactInfos)

        val result = route(application, request).value

        val exception = intercept[BadRequestException] {
          await(result)
        }
        exception.message mustBe "The ContactCheckYourAnswersForm submitted is out of date"
      }
    }
    "must throw BadRequestException when the request contains an invalid form" in {
      val request = FakeRequest(POST, routes.ContactCheckYourAnswersController.saveAndContinue().url)
        .withFormUrlEncodedBody(
          "contacts[0].name"  -> "",
          "contacts[0].email" -> ""
        )
      val testContactInfos = List(ContactInfo("", ""))
      val application      = applicationBuilder(userAnswers = Some(testUserAnswers)).build()
      running(application) {
        val mockContactCheckYourAnswersService = application.injector.instanceOf[ContactCheckYourAnswersService]
        when(mockContactCheckYourAnswersService.getContactInfos(meq(testUserAnswers))).thenReturn(testContactInfos)

        val result = route(application, request).value

        val exception = intercept[BadRequestException] {
          await(result)
        }
        exception.message mustBe "invalid ContactCheckYourAnswersForm submitted"
      }
    }
  }
}
