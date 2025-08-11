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
import views.html.ContactCheckYourAnswersView
import models.{ContactInfo, UserAnswers}
import org.mockito.ArgumentMatchers.{eq => meq, any}
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.guice.GuiceApplicationBuilder
import services.ContactCheckYourAnswersService
import play.api.inject.bind
import uk.gov.hmrc.http.BadRequestException
import repositories.SessionRepository
import scala.concurrent.Future
import pages.ContactsPage

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
        val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()

        running(application) {
          val testContactInfos                   = List(ContactInfo("", "", "", ""))
          val mockContactCheckYourAnswersService = application.injector.instanceOf[ContactCheckYourAnswersService]
          when(mockContactCheckYourAnswersService.getContactInfos(meq(testUserAnswers))).thenReturn(testContactInfos)
          val request = FakeRequest(GET, routes.ContactCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ContactCheckYourAnswersView]
          status(result) mustEqual OK
          contentAsString(result) mustEqual view(testContactInfos)(
            request,
            messages(application)
          ).toString
        }
      }

      "must redirect to journey recovery when no contacts found" in {
        val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()
        running(application) {
          val mockContactCheckYourAnswersService = application.injector.instanceOf[ContactCheckYourAnswersService]
          when(mockContactCheckYourAnswersService.getContactInfos(meq(testUserAnswers))).thenReturn(List.empty)
          val request = FakeRequest(GET, routes.ContactCheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustEqual Some(routes.JourneyRecoveryController.onPageLoad().url)
        }
      }

      "must redirect to index when contacts have been submitted" in {
        val application = applicationBuilder(userAnswers = Some(completedUserAnswers)).build()
        running(application) {
          val request = FakeRequest(GET, routes.ContactCheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustEqual Some(routes.IndexController.onPageLoad().url)
        }
      }
    }

    "saveAndContinue endpoint:" - {
      "must redirect to index controller when record saved" - {
        "Data saved to the SessionRepository must be the sanitised contacts" in {
          val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()
          running(application) {

            val testContactInfos = List(ContactInfo("name", "role", "email", "phone"))

            val mockedSessionRepository = application.injector.instanceOf[SessionRepository]
            when(mockedSessionRepository.set(any())).thenReturn(Future.successful(true))
            val mockContactCheckYourAnswersService = application.injector.instanceOf[ContactCheckYourAnswersService]
            when(mockContactCheckYourAnswersService.getContactInfos(meq(testUserAnswers))).thenReturn(testContactInfos)

            val request = FakeRequest(POST, routes.ContactCheckYourAnswersController.saveAndContinue().url)
              .withFormUrlEncodedBody(
                "contacts[0].name"  -> "name",
                "contacts[0].role"  -> "role",
                "contacts[0].email" -> "email",
                "contacts[0].phone" -> "phone"
              )
            val result = route(application, request).value
            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustEqual Some(routes.IndexController.onPageLoad().url)
            withClue("verify that the mongo instance was called once to update with bound Page -> UserAnswers\n") {
              verify(mockedSessionRepository, times(1)).set(testUserAnswers.set(ContactsPage, testContactInfos).get)
            }
          }
        }
      }
    }

    "must throw BadRequestException when service contactInfo and form are misaligned" in {
      val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()
      running(application) {
        val testContactInfos                   = List(ContactInfo("name", "role", "email", "phone"))
        val mockContactCheckYourAnswersService = application.injector.instanceOf[ContactCheckYourAnswersService]
        when(mockContactCheckYourAnswersService.getContactInfos(meq(testUserAnswers))).thenReturn(testContactInfos)
        val request = FakeRequest(POST, routes.ContactCheckYourAnswersController.saveAndContinue().url)
          .withFormUrlEncodedBody(
            "contacts[0].name"  -> "differentName",
            "contacts[0].role"  -> "anotherRole",
            "contacts[0].email" -> "stolenEmail",
            "contacts[0].phone" -> "newPhone"
          )
        val result    = route(application, request).value
        val exception = intercept[BadRequestException] {
          await(result)
        }
        exception.message mustBe "The ContactCheckYourAnswersForm submitted is out of date"
      }
    }
    "must throw BadRequestException when the request contains an invalid form" in {
      val application = applicationBuilder(userAnswers = Some(testUserAnswers)).build()
      running(application) {
        val testContactInfos                   = List(ContactInfo("", "", "", ""))
        val mockContactCheckYourAnswersService = application.injector.instanceOf[ContactCheckYourAnswersService]
        when(mockContactCheckYourAnswersService.getContactInfos(meq(testUserAnswers))).thenReturn(testContactInfos)
        val request = FakeRequest(POST, routes.ContactCheckYourAnswersController.saveAndContinue().url)
          .withFormUrlEncodedBody(
            "contacts[0].name"  -> "",
            "contacts[0].role"  -> "",
            "contacts[0].email" -> "",
            "contacts[0].phone" -> ""
          )
        val result    = route(application, request).value
        val exception = intercept[BadRequestException] {
          await(result)
        }
        exception.message mustBe "invalid ContactCheckYourAnswersForm submitted"
      }
    }
  }
}
