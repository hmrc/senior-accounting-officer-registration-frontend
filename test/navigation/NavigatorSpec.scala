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

package navigation

import base.SpecBase
import config.FeatureToggleSupport
import controllers.routes
import models.ContactType.*
import models.config.FeatureToggle
import models.config.FeatureToggle.ContactFlowReshuffle
import models.{config, *}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages.*

class NavigatorSpec extends SpecBase with FeatureToggleSupport with BeforeAndAfterEach with GuiceOneAppPerSuite {

  private val navigator = app.injector.instanceOf[Navigator]

  override def beforeEach(): Unit = {
    disable(ContactFlowReshuffle)
  }

  override def afterEach(): Unit = {
    disable(ContactFlowReshuffle)
  }

  "Navigator" - {

    Seq(false, true).foreach { reshuffled =>
      Seq(ContactHaveYouAddedAll.Yes, ContactHaveYouAddedAll.No).foreach { answer =>
        s"must route $answer independently in normal mode with reshuffle=$reshuffled" in {
          if reshuffled then enable(ContactFlowReshuffle)
          val answers     = emptyUserAnswers.set(ContactHaveYouAddedAllPage(First, NormalMode), answer).get
          val wantsSecond =
            if reshuffled then answer == ContactHaveYouAddedAll.Yes else answer == ContactHaveYouAddedAll.No
          val expected =
            if wantsSecond then routes.ContactNameController.onPageLoad(Second, NormalMode)
            else if reshuffled then routes.ContactCheckYourAnswersController.onPageLoadReshuffled()
            else routes.IndexController.onPageLoad()
          navigator.nextPage(ContactHaveYouAddedAllPage(First, NormalMode), NormalMode, answers) mustBe expected
        }
      }
    }

    "in Normal mode with feature switch off" - {
      "must go from a page that doesn't exist in the route map to Index" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }

      "must go from contact email to first contact CYA" in {
        navigator.nextPage(
          ContactEmailPage(First, NormalMode),
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.ContactCheckYourAnswersController.onPageLoadLegacy(First)
      }

      "must go from first contact CYA to add another page" in {
        navigator.nextPage(
          ContactCheckYourAnswersPage(First),
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.ContactHaveYouAddedAllController.onPageLoad(First, NormalMode)
      }

      "must go from second contact email to second contact CYA" in {
        navigator.nextPage(
          ContactEmailPage(Second, NormalMode),
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.ContactCheckYourAnswersController.onPageLoadLegacy(Second)
      }

      "must go from second contact CYA to index" in {
        navigator.nextPage(
          ContactCheckYourAnswersPage(Second),
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.IndexController.onPageLoad()
      }
    }

    "in Check mode with feature switch off" - {
      "must return first contact name changes to first contact CYA" in {
        navigator.nextPage(
          ContactNamePage(First, NormalMode),
          CheckMode,
          UserAnswers("id")
        ) mustBe routes.ContactCheckYourAnswersController.onPageLoadLegacy(First)
      }

      "must return second contact email changes to second contact CYA" in {
        navigator.nextPage(
          ContactEmailPage(Second, NormalMode),
          CheckMode,
          UserAnswers("id")
        ) mustBe routes.ContactCheckYourAnswersController.onPageLoadLegacy(Second)
      }
    }

    "in Normal mode with feature switch on" - {
      "must go from first contact email to add another page" in {
        enable(ContactFlowReshuffle)
        navigator.nextPage(
          ContactEmailPage(First, NormalMode),
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.ContactHaveYouAddedAllController.onPageLoad(First, NormalMode)
      }

      "must go from add another yes to second contact name" in {
        enable(ContactFlowReshuffle)
        navigator.nextPage(
          ContactHaveYouAddedAllPage(First, NormalMode),
          NormalMode,
          UserAnswers("id").set(ContactHaveYouAddedAllPage(First, NormalMode), ContactHaveYouAddedAll.Yes).get
        ) mustBe routes.ContactNameController.onPageLoad(Second, NormalMode)
      }

      "must go from second contact email to combined CYA" in {
        enable(ContactFlowReshuffle)
        navigator.nextPage(
          ContactEmailPage(Second, NormalMode),
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.ContactCheckYourAnswersController.onPageLoadReshuffled()
      }

      "must go from combined CYA to index" in {
        enable(ContactFlowReshuffle)
        navigator.nextPage(
          ContactsCheckYourAnswersPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.IndexController.onPageLoad()
      }
    }

    "in Check mode with feature switch on" - {
      "must return field changes to combined CYA" in {
        enable(ContactFlowReshuffle)
        navigator.nextPage(
          ContactEmailPage(First, NormalMode),
          CheckMode,
          UserAnswers("id")
        ) mustBe routes.ContactCheckYourAnswersController.onPageLoadReshuffled()
      }

      "must return add another no to combined CYA" in {
        enable(ContactFlowReshuffle)
        navigator.nextPage(
          ContactHaveYouAddedAllPage(First, NormalMode),
          CheckMode,
          UserAnswers("id").set(ContactHaveYouAddedAllPage(First, NormalMode), ContactHaveYouAddedAll.No).get
        ) mustBe routes.ContactCheckYourAnswersController.onPageLoadReshuffled()
      }

      "must return add another yes with existing second contact to combined CYA" in {
        enable(ContactFlowReshuffle)
        navigator.nextPage(
          ContactHaveYouAddedAllPage(First, NormalMode),
          CheckMode,
          UserAnswers("id")
            .set(ContactHaveYouAddedAllPage(First, NormalMode), ContactHaveYouAddedAll.Yes)
            .get
            .set(ContactNamePage(Second, NormalMode), "name")
            .get
            .set(ContactEmailPage(Second, NormalMode), "email")
            .get
        ) mustBe routes.ContactCheckYourAnswersController.onPageLoadReshuffled()
      }

      Seq(ContactNamePage(Second, NormalMode), ContactEmailPage(Second, NormalMode)).foreach { page =>
        s"must collect second contact details when yes is selected and only $page exists" in {
          enable(ContactFlowReshuffle)
          val answers = emptyUserAnswers
            .set(ContactHaveYouAddedAllPage(First, NormalMode), ContactHaveYouAddedAll.Yes)
            .get
            .set(page, "existing value")
            .get
          navigator.nextPage(ContactHaveYouAddedAllPage(First, NormalMode), CheckMode, answers) mustBe
            routes.ContactNameController.onPageLoad(Second, NormalMode)
        }
      }

      "must return no with retained second contact details to combined CYA" in {
        enable(ContactFlowReshuffle)
        val answers = emptyUserAnswers
          .set(ContactHaveYouAddedAllPage(First, NormalMode), ContactHaveYouAddedAll.No)
          .get
          .set(ContactNamePage(Second, NormalMode), "name")
          .get
          .set(ContactEmailPage(Second, NormalMode), "email")
          .get
        navigator.nextPage(ContactHaveYouAddedAllPage(First, NormalMode), CheckMode, answers) mustBe
          routes.ContactCheckYourAnswersController.onPageLoadReshuffled()
      }

      "must return add another yes without second contact to second contact name" in {
        enable(ContactFlowReshuffle)
        navigator.nextPage(
          ContactHaveYouAddedAllPage(First, NormalMode),
          CheckMode,
          UserAnswers("id").set(ContactHaveYouAddedAllPage(First, NormalMode), ContactHaveYouAddedAll.Yes).get
        ) mustBe routes.ContactNameController.onPageLoad(Second, NormalMode)
      }
    }
  }
}
