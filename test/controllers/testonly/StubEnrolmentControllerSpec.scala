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

package controllers.testonly

import base.SpecBase
import config.AppConfig
import connectors.EnrolmentStoreStubConnector
import controllers.testonly.StubEnrolmentControllerSpec.*
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.libs.json.JsObject
import play.api.mvc.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Retrieval, ~}
import uk.gov.hmrc.auth.core.{AuthConnector, MissingBearerToken}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class StubEnrolmentControllerSpec extends SpecBase with GuiceOneAppPerSuite with MockitoSugar {

  private val authConnector               = mock[AuthConnector]
  private val enrolmentStoreStubConnector = mock[EnrolmentStoreStubConnector]

  override def fakeApplication(): Application =
    applicationBuilder()
      .configure(Map("application.router" -> "testOnlyDoNotUseInAppConf.Routes"))
      .overrides(
        bind[AuthConnector].toInstance(authConnector),
        bind[EnrolmentStoreStubConnector].toInstance(enrolmentStoreStubConnector)
      )
      .build()

  "StubEnrolmentController.stubEnrolment" - {

    "must redirect unauthenticated users to auth wizard with a continue URL back to the test-only route" in {
      mockAuth(Future.failed(new MissingBearerToken))

      val request = FakeRequest(GET, controllers.testonly.routes.StubEnrolmentController.stubEnrolment().url)
      val result  = route(app, request).value

      val appConfig = app.injector.instanceOf[AppConfig]

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value must startWith(appConfig.loginUrl)
      redirectLocation(result).value must include(
        "continue=http%3A%2F%2Flocalhost%3A10057%2Fsenior-accounting-officer%2Fregistration%2Ftest-only%2Fstub-enrolment"
      )
    }

    "must configure enrolment-store-stub and redirect authenticated users to the registration start" in {
      mockAuth(successfulAuthResponse)
      when(enrolmentStoreStubConnector.upsertGroupPersona(any(), any())(using any()))
        .thenReturn(Future.successful(true))

      val request = FakeRequest(GET, controllers.testonly.routes.StubEnrolmentController.stubEnrolment().url)
      val result  = route(app, request).value

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe controllers.routes.IndexController.onPageLoad().url

      val (groupId, persona) = captureUpsert()
      groupId mustBe testGroupId
      assertDsaoPersona(persona)
    }

    "must return InternalServerError when enrolment-store-stub configuration fails" in {
      mockAuth(successfulAuthResponse)
      when(enrolmentStoreStubConnector.upsertGroupPersona(any(), any())(using any()))
        .thenReturn(Future.successful(false))

      val request = FakeRequest(GET, controllers.testonly.routes.StubEnrolmentController.stubEnrolment().url)
      val result  = route(app, request).value

      status(result) mustBe INTERNAL_SERVER_ERROR
    }

    "must not resolve from the production router" in {
      val application = applicationBuilder().build()

      running(application) {
        val request = FakeRequest(
          GET,
          "/senior-accounting-officer/registration/test-only/stub-enrolment"
        )

        val result = route(application, request).value

        status(result) mustBe NOT_FOUND
      }
    }
  }

  def mockAuth(response: Future[Any]): Unit =
    when(authConnector.authorise(any[Predicate], any[Retrieval[Any]])(using any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(response)

  def captureUpsert(): (String, JsObject) = {
    val groupIdCaptor = ArgumentCaptor.forClass(classOf[String])
    val personaCaptor = ArgumentCaptor.forClass(classOf[JsObject])

    verify(enrolmentStoreStubConnector).upsertGroupPersona(groupIdCaptor.capture(), personaCaptor.capture())(using
      any()
    )

    groupIdCaptor.getValue -> personaCaptor.getValue
  }

  def assertDsaoPersona(json: JsObject): Unit = {
    (json \ "groupId").as[String] mustBe testGroupId
    (json \ "affinityGroup").as[String] mustBe "Organisation"
    (json \ "users" \ 0 \ "credId").as[String] mustBe testProviderId
    (json \ "enrolments" \ 0 \ "serviceName").as[String] mustBe "HMRC-DSAO-ORG"
    (json \ "enrolments" \ 0 \ "identifiers" \ 0 \ "key").as[String] mustBe "EtmpSubscriptionId"
    (json \ "enrolments" \ 0 \ "state").as[String] mustBe "Activated"
    (json \ "enrolments" \ 0 \ "enrolmentType").as[String] mustBe "principal"
    (json \ "enrolments" \ 0 \ "assignedUserCreds" \ 0).as[String] mustBe testProviderId
  }
}

object StubEnrolmentControllerSpec {
  val testProviderId: String = "test-provider-id"
  val testGroupId: String    = s"sao-registration-$testProviderId"

  val successfulAuthResponse: Future[Any] =
    Future.successful(new ~(Some("internal-id"), Some(Credentials(testProviderId, "GovernmentGateway"))))
}
