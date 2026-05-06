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

package navigation

import base.SpecBase
import controllers.routes
import models.*
import models.ContactType.*
import pages.*

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }

      "when the user is in the add nominated company details journey" - {
        "must go from nominated company guidance page to GRS stubs" in {
          navigator.nextPage(
            NominatedCompanyDetailsGuidancePage,
            NormalMode,
            UserAnswers("id")
          ) mustBe routes.GrsController.start()
        }
      }

      "when the user is in the add first contact details journey" - {
        "must go from contact name to contact email" in {
          navigator.nextPage(
            ContactNamePage(First),
            NormalMode,
            UserAnswers("id")
          ) mustBe routes.ContactEmailController
            .onPageLoad(First, NormalMode)
        }

        "must go from contact email to check your answers for first contact" in {
          navigator.nextPage(
            ContactEmailPage(First),
            NormalMode,
            UserAnswers("id")
          ) mustBe routes.ContactCheckYourAnswersController
            .onPageLoad(First)
        }

        "must go from check your answers for first contact to add another page" in {
          navigator.nextPage(
            ContactCheckYourAnswersPage(First),
            NormalMode,
            UserAnswers("id")
          ) mustBe routes.ContactHaveYouAddedAllController
            .onPageLoad(First)
        }

        "on add another page" - {
          "when the user answers Yes must go to index page" in {
            navigator.nextPage(
              ContactHaveYouAddedAllPage(First),
              NormalMode,
              UserAnswers("id").set(ContactHaveYouAddedAllPage(First), ContactHaveYouAddedAll.Yes).get
            ) mustBe routes.IndexController.onPageLoad()
          }
          "when the user answers No must go to 2nd contact name" in {
            navigator.nextPage(
              ContactHaveYouAddedAllPage(First),
              NormalMode,
              UserAnswers("id").set(ContactHaveYouAddedAllPage(First), ContactHaveYouAddedAll.No).get
            ) mustBe routes.ContactNameController
              .onPageLoad(Second, NormalMode)
          }
        }
      }

      "when the user is in the add second contact details journey" - {
        "must go from contact name to contact email" in {
          navigator.nextPage(
            ContactNamePage(Second),
            NormalMode,
            UserAnswers("id")
          ) mustBe routes.ContactEmailController
            .onPageLoad(Second, NormalMode)
        }

        "must go from contact email to check your answers page for second contact" in {
          navigator.nextPage(
            ContactEmailPage(Second),
            NormalMode,
            UserAnswers("id")
          ) mustBe routes.ContactCheckYourAnswersController
            .onPageLoad(Second)
        }

        "must go from check your answers page for second contact to index page" in {
          navigator.nextPage(
            ContactCheckYourAnswersPage(Second),
            NormalMode,
            UserAnswers("id")
          ) mustBe routes.IndexController.onPageLoad()
        }
      }
    }

    "in Check mode" - {
      "when the user is in the add first contact details journey" - {
        "must go from first contact name page to contact check your answers page for first contact" in {
          navigator.nextPage(
            ContactNamePage(First),
            CheckMode,
            UserAnswers("id")
          ) mustBe routes.ContactCheckYourAnswersController
            .onPageLoad(First)
        }

        "must go from first contact email page to contact check your answers page for first contact" in {
          navigator.nextPage(
            ContactEmailPage(First),
            CheckMode,
            UserAnswers("id")
          ) mustBe routes.ContactCheckYourAnswersController
            .onPageLoad(First)
        }
      }
      "when the user is in the add second contact details journey" - {
        "must go from second contact name page to contact check your answers page for second contact" in {
          navigator.nextPage(
            ContactNamePage(Second),
            CheckMode,
            UserAnswers("id")
          ) mustBe routes.ContactCheckYourAnswersController
            .onPageLoad(Second)
        }

        "must go from second contact email page to contact check your answers page for second contact" in {
          navigator.nextPage(
            ContactEmailPage(Second),
            CheckMode,
            UserAnswers("id")
          ) mustBe routes.ContactCheckYourAnswersController
            .onPageLoad(Second)
        }
      }
    }
  }
}
