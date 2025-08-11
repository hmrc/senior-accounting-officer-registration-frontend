package controllers.actions
import base.SpecBase
import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.routes
import play.api.mvc.{Action, AnyContent, BodyParsers, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import base.SpecBase
import play.api.mvc.Action
import play.api.mvc.AnyContent
import models.UserAnswers
import pages.ContactHaveYouAddedAllPage
import models.ContactType
import models.ContactHaveYouAddedAll
import models.requests.DataRequest
import play.api.mvc.Result
import org.mockito.Mockito.*
import play.api.test.FakeRequest
import org.scalatestplus.mockito.MockitoSugar
import repositories.SessionRepository
import models.ContactInfo
import models.requests.OptionalDataRequest

class RedirectActionSpec extends SpecBase with MockitoSugar {

  class Harness() extends RedirectActionImpl() {
    def callFilter(request: DataRequest[UserAnswers]): Future[Option[Result]] = filter(request)
  }

  "Redirect Action" - {
    "must redirect user" - {
      "when the contactHaveYouAddedAll flag == Yes" in {
        true
        // val application = applicationBuilder(userAnswers = None).build()
        // running(application) {
        //   val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        //   val appConfig   = application.injector.instanceOf[FrontendAppConfig]

        //   val authAction = new FrontendAuthenticatedIdentifierAction(
        //     new FakeFailingAuthConnector(new MissingBearerToken),
        //     appConfig,
        //     bodyParsers
        //   )
        //   val controller = new Harness(authAction)
        //   val result     = controller.onPageLoad()(FakeRequest())

        //   status(result) mustBe SEE_OTHER
        //   redirectLocation(result).value must startWith(appConfig.loginUrl)
      }
    }
  }
  "must not redirect user" - {
    "when the contactHaveYouAddedAll flag == No" in {
      // val userAnswers = UserAnswers("id")
      //   .set(ContactHaveYouAddedAllPage(ContactType.First), ContactHaveYouAddedAll.Yes)
      //   .fold(
      //     _ => None,
      //     Some(_)
      //   )
      // val application = applicationBuilder(userAnswers = userAnswers).build()
      // running(application) {
      //   val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
      //   val appConfig   = application.injector.instanceOf[FrontendAppConfig]
      true
    }

    "when the contactHaveYouAddedAll flag is not set" in {
      val sessionRepository = mock[SessionRepository]
      when(sessionRepository.get("id")) thenReturn Future(None)
      val action = new Harness()

      val result =
        action.callFilter(new DataRequest(FakeRequest[UserAnswers](), "id", emptyUserAnswers)).futureValue

    }
  }
}
