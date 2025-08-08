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

class RedirectActionSpec extends SpecBase {

  class Harness(authAction: RedirectAction) {
    // def onPageLoad(): Action[AnyContent] =
  }

  "Redirect Action" - {
    "when the contactHaveYouAddedAll == Yes" - {
      "must not redirect user" in {
        val application = applicationBuilder(userAnswers = None).build()
        running(application) {
        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val appConfig   = application.injector.instanceOf[FrontendAppConfig]

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
    "when the contactHaveYouAddedAll == No" - {
      "must redirect user" in {
        val userAnswers = UserAnswers("id").set(ContactHaveYouAddedAllPage(ContactType.First), ContactHaveYouAddedAll.Yes).get
        val application = applicationBuilder(userAnswers = userAnswers).build()
        running(application) {
        val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
        val appConfig   = application.injector.instanceOf[FrontendAppConfig]

      }
    }
  }
}
