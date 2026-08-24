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

package controllers.actions

import base.SpecBase
import com.google.inject.Inject
import config.AppConfig
import controllers.actions.AuthenticatedIdentifierAction.DsaoEnrolmentKey
import controllers.routes
import play.api.mvc.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AuthActionSpec extends SpecBase {

  class Harness(authAction: IdentifierAction) {
    def onPageLoad(): Action[AnyContent] = authAction { _ => Results.Ok }
  }

  private def retrievals(affinityGroup: AffinityGroup, enrolmentKeys: String*) =
    new ~(
      new ~(Some("internalId"), Some(affinityGroup)),
      Enrolments(enrolmentKeys.map(key => Enrolment(key)).toSet)
    )

  "Auth Action" - {

    "when the user hasn't logged in" - {

      "must redirect the user to log in " in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[AppConfig]

          val authAction = new FrontendAuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new MissingBearerToken),
            appConfig,
            bodyParsers
          )
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value must startWith(appConfig.loginUrl)
        }
      }
    }

    "the user's session has expired" - {

      "must redirect the user to log in " in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[AppConfig]

          val authAction = new FrontendAuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new BearerTokenExpired),
            appConfig,
            bodyParsers
          )
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value must startWith(appConfig.loginUrl)
        }
      }
    }

    "the user doesn't have sufficient enrolments" - {

      "must redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[AppConfig]

          val authAction = new FrontendAuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new InsufficientEnrolments),
            appConfig,
            bodyParsers
          )
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad().url
        }
      }
    }

    "the user doesn't have sufficient confidence level" - {

      "must redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[AppConfig]

          val authAction = new FrontendAuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new InsufficientConfidenceLevel),
            appConfig,
            bodyParsers
          )
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad().url
        }
      }
    }

    "the user used an unaccepted auth provider" - {

      "must redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[AppConfig]

          val authAction = new FrontendAuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new UnsupportedAuthProvider),
            appConfig,
            bodyParsers
          )
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad().url
        }
      }
    }

    "the user has an unsupported affinity group" - {

      "must redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[AppConfig]

          val authAction = new FrontendAuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new UnsupportedAffinityGroup),
            appConfig,
            bodyParsers
          )
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
        }
      }
    }

    "the user has an Individual affinity group" - {

      "must redirect the user to the cannot access service kick-out page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[AppConfig]

          val authAction = new FrontendAuthenticatedIdentifierAction(
            new FakeSuccessfulAuthConnector(retrievals(AffinityGroup.Individual)),
            appConfig,
            bodyParsers
          )
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.CannotAccessServiceController.onPageLoad().url)
        }
      }
    }

    "the user has an Agent affinity group" - {

      "must redirect the user to the agent cannot access service kick-out page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[AppConfig]

          val authAction = new FrontendAuthenticatedIdentifierAction(
            new FakeSuccessfulAuthConnector(retrievals(AffinityGroup.Agent)),
            appConfig,
            bodyParsers
          )
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.AgentCannotAccessServiceController.onPageLoad().url)
        }
      }
    }

    "the user has an Organisation affinity group" - {

      "must let the user through to the requested page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[AppConfig]

          val authAction = new FrontendAuthenticatedIdentifierAction(
            new FakeSuccessfulAuthConnector(retrievals(AffinityGroup.Organisation)),
            appConfig,
            bodyParsers
          )
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe OK
        }
      }
    }

    "the user already holds the DSAO enrolment" - {

      "must redirect the user to the already registered kick-out page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[AppConfig]

          val authAction = new FrontendAuthenticatedIdentifierAction(
            new FakeSuccessfulAuthConnector(retrievals(AffinityGroup.Organisation, DsaoEnrolmentKey)),
            appConfig,
            bodyParsers
          )
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.AlreadyRegisteredController.onPageLoad().url)
        }
      }
    }

    "the user holds an enrolment other than DSAO" - {

      "must let the user through" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[AppConfig]

          val authAction = new FrontendAuthenticatedIdentifierAction(
            new FakeSuccessfulAuthConnector(retrievals(AffinityGroup.Organisation, "IR-SA")),
            appConfig,
            bodyParsers
          )
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe OK
        }
      }
    }

    "the user has an unsupported credential role" - {

      "must redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[AppConfig]

          val authAction = new FrontendAuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new UnsupportedCredentialRole),
            appConfig,
            bodyParsers
          )
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
        }
      }
    }
  }
}

class FakeSuccessfulAuthConnector[T] @Inject() (retrievalToReturn: T) extends AuthConnector {
  val serviceUrl: String = ""

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(using
      hc: HeaderCarrier,
      ec: ExecutionContext
  ): Future[A] =
    Future.successful(retrievalToReturn.asInstanceOf[A])
}

class FakeFailingAuthConnector @Inject() (exceptionToReturn: Throwable) extends AuthConnector {
  val serviceUrl: String = ""

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(using
      hc: HeaderCarrier,
      ec: ExecutionContext
  ): Future[A] =
    Future.failed(exceptionToReturn)
}
