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
import controllers.routes
import models.*
import models.ContactType.*
import pages.*
import play.api.Configuration

class NavigatorSpec extends SpecBase {

  private val oldFlowNavigator = new Navigator(Configuration.from(Map("features.contactFlowReshuffle" -> false)))
  private val newFlowNavigator = new Navigator(Configuration.from(Map("features.contactFlowReshuffle" -> true)))

  "Navigator" - {

    "in Normal mode with feature switch off" - {
      "must go from a page that doesn't exist in the route map to Index" in {
        case object UnknownPage extends Page
        oldFlowNavigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }

      "must go from contact email to first contact CYA" in {
        oldFlowNavigator.nextPage(
          ContactEmailPage(First),
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.ContactCheckYourAnswersController.onPageLoadLegacy(First)
      }

      "must go from first contact CYA to add another page" in {
        oldFlowNavigator.nextPage(
          ContactCheckYourAnswersPage(First),
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.ContactHaveYouAddedAllController.onPageLoad(First, NormalMode)
      }

      "must go from second contact email to second contact CYA" in {
        oldFlowNavigator.nextPage(
          ContactEmailPage(Second),
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.ContactCheckYourAnswersController.onPageLoadLegacy(Second)
      }

      "must go from second contact CYA to index" in {
        oldFlowNavigator.nextPage(
          ContactCheckYourAnswersPage(Second),
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.IndexController.onPageLoad()
      }
    }

    "in Check mode with feature switch off" - {
      "must return first contact name changes to first contact CYA" in {
        oldFlowNavigator.nextPage(
          ContactNamePage(First),
          CheckMode,
          UserAnswers("id")
        ) mustBe routes.ContactCheckYourAnswersController.onPageLoadLegacy(First)
      }

      "must return second contact email changes to second contact CYA" in {
        oldFlowNavigator.nextPage(
          ContactEmailPage(Second),
          CheckMode,
          UserAnswers("id")
        ) mustBe routes.ContactCheckYourAnswersController.onPageLoadLegacy(Second)
      }
    }

    "in Normal mode with feature switch on" - {
      "must go from first contact email to add another page" in {
        newFlowNavigator.nextPage(
          ContactEmailPage(First),
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.ContactHaveYouAddedAllController.onPageLoad(First, NormalMode)
      }

      "must go from add another yes to combined CYA" in {
        newFlowNavigator.nextPage(
          ContactHaveYouAddedAllPage(First),
          NormalMode,
          UserAnswers("id").set(ContactHaveYouAddedAllPage(First), ContactHaveYouAddedAll.Yes).get
        ) mustBe routes.ContactCheckYourAnswersController.onPageLoadReshuffled()
      }

      "must go from second contact email to combined CYA" in {
        newFlowNavigator.nextPage(
          ContactEmailPage(Second),
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.ContactCheckYourAnswersController.onPageLoadReshuffled()
      }

      "must go from combined CYA to index" in {
        newFlowNavigator.nextPage(
          ContactsCheckYourAnswersPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.IndexController.onPageLoad()
      }
    }

    "in Check mode with feature switch on" - {
      "must return field changes to combined CYA" in {
        newFlowNavigator.nextPage(
          ContactEmailPage(First),
          CheckMode,
          UserAnswers("id")
        ) mustBe routes.ContactCheckYourAnswersController.onPageLoadReshuffled()
      }

      "must return add another yes to combined CYA" in {
        newFlowNavigator.nextPage(
          ContactHaveYouAddedAllPage(First),
          CheckMode,
          UserAnswers("id").set(ContactHaveYouAddedAllPage(First), ContactHaveYouAddedAll.Yes).get
        ) mustBe routes.ContactCheckYourAnswersController.onPageLoadReshuffled()
      }

      "must return add another no with existing second contact to combined CYA" in {
        newFlowNavigator.nextPage(
          ContactHaveYouAddedAllPage(First),
          CheckMode,
          UserAnswers("id")
            .set(ContactHaveYouAddedAllPage(First), ContactHaveYouAddedAll.No)
            .get
            .set(ContactNamePage(Second), "name")
            .get
            .set(ContactEmailPage(Second), "email")
            .get
        ) mustBe routes.ContactCheckYourAnswersController.onPageLoadReshuffled()
      }

      "must return add another no without second contact to second contact name" in {
        newFlowNavigator.nextPage(
          ContactHaveYouAddedAllPage(First),
          CheckMode,
          UserAnswers("id").set(ContactHaveYouAddedAllPage(First), ContactHaveYouAddedAll.No).get
        ) mustBe routes.ContactNameController.onPageLoad(Second, NormalMode)
      }
    }
  }
}
