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
import models.*
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages.*

class NavigatorSpec extends SpecBase with FeatureToggleSupport with BeforeAndAfterEach with GuiceOneAppPerSuite {

  private val navigator = app.injector.instanceOf[Navigator]

  "Navigator" - {

    "normal mode" - {

      Seq(ContactHaveYouAddedAll.Yes, ContactHaveYouAddedAll.No).foreach { answer =>
        s"must route $answer independently in normal mode" in {
          val answers     = emptyUserAnswers.set(ContactHaveYouAddedAllPage(NormalMode), answer).get
          val wantsSecond = answer == ContactHaveYouAddedAll.Yes
          val expected    =
            if wantsSecond then routes.ContactNameController.onPageLoad(Second, NormalMode)
            else routes.ContactCheckYourAnswersController.onPageLoad()
          navigator.nextPage(ContactHaveYouAddedAllPage(NormalMode), NormalMode, answers) mustBe expected
        }
      }

      "must go from first contact name to first contact email page" in {
        navigator.nextPage(
          ContactNamePage(First, NormalMode),
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.ContactEmailController.onPageLoad(First, NormalMode)
      }

      "must go from first contact email to add another page" in {
        navigator.nextPage(
          ContactEmailPage(First, NormalMode),
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.ContactHaveYouAddedAllController.onPageLoad(NormalMode)
      }

      "must go from add another yes to second contact name" in {
        navigator.nextPage(
          ContactHaveYouAddedAllPage(NormalMode),
          NormalMode,
          UserAnswers("id").set(ContactHaveYouAddedAllPage(NormalMode), ContactHaveYouAddedAll.Yes).get
        ) mustBe routes.ContactNameController.onPageLoad(Second, NormalMode)
      }

      "must go from second contact email to CYA" in {
        navigator.nextPage(
          ContactEmailPage(Second, NormalMode),
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.ContactCheckYourAnswersController.onPageLoad()
      }

      "must go from CYA to index" in {
        navigator.nextPage(
          ContactsCheckYourAnswersPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.IndexController.onPageLoad()
      }

      "must go from a page that doesn't exist in the route map to Index" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }
    }

    "check mode" - {

      "must return name field changes to CYA" in {
        navigator.nextPage(
          ContactNamePage(First, NormalMode),
          CheckMode,
          UserAnswers("id")
        ) mustBe routes.ContactCheckYourAnswersController.onPageLoad()
      }

      "must return email field changes to CYA" in {
        navigator.nextPage(
          ContactEmailPage(First, NormalMode),
          CheckMode,
          UserAnswers("id")
        ) mustBe routes.ContactCheckYourAnswersController.onPageLoad()
      }

      "must return add another no to CYA" in {

        navigator.nextPage(
          ContactHaveYouAddedAllPage(NormalMode),
          CheckMode,
          UserAnswers("id").set(ContactHaveYouAddedAllPage(NormalMode), ContactHaveYouAddedAll.No).get
        ) mustBe routes.ContactCheckYourAnswersController.onPageLoad()
      }

      "must return add another yes with existing second contact to CYA" in {

        navigator.nextPage(
          ContactHaveYouAddedAllPage(NormalMode),
          CheckMode,
          UserAnswers("id")
            .set(ContactHaveYouAddedAllPage(NormalMode), ContactHaveYouAddedAll.Yes)
            .get
            .set(ContactNamePage(Second, NormalMode), "name")
            .get
            .set(ContactEmailPage(Second, NormalMode), "email")
            .get
        ) mustBe routes.ContactCheckYourAnswersController.onPageLoad()
      }

      Seq(ContactNamePage(Second, NormalMode), ContactEmailPage(Second, NormalMode)).foreach { page =>
        s"must collect second contact details when yes is selected and only $page exists" in {

          val answers = emptyUserAnswers
            .set(ContactHaveYouAddedAllPage(NormalMode), ContactHaveYouAddedAll.Yes)
            .get
            .set(page, "existing value")
            .get
          navigator.nextPage(ContactHaveYouAddedAllPage(NormalMode), CheckMode, answers) mustBe
            routes.ContactNameController.onPageLoad(Second, NormalMode)
        }
      }

      "must return no with retained second contact details to CYA" in {

        val answers = emptyUserAnswers
          .set(ContactHaveYouAddedAllPage(NormalMode), ContactHaveYouAddedAll.No)
          .get
          .set(ContactNamePage(Second, NormalMode), "name")
          .get
          .set(ContactEmailPage(Second, NormalMode), "email")
          .get
        navigator.nextPage(ContactHaveYouAddedAllPage(NormalMode), CheckMode, answers) mustBe
          routes.ContactCheckYourAnswersController.onPageLoad()
      }

      "must return add another yes without second contact to second contact name" in {

        navigator.nextPage(
          ContactHaveYouAddedAllPage(NormalMode),
          CheckMode,
          UserAnswers("id").set(ContactHaveYouAddedAllPage(NormalMode), ContactHaveYouAddedAll.Yes).get
        ) mustBe routes.ContactNameController.onPageLoad(Second, NormalMode)
      }

      "must go from a page that doesn't exist in the route map to Index" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }
    }

    "transaction mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, TransactionMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }

      "on ContactHaveYouAddedAllPage" - {
        "there was no previous answer" - {
          "must throw an exception" in {

            val userAnswers =
              emptyUserAnswers.add(ContactHaveYouAddedAllPage(TransactionMode), ContactHaveYouAddedAll.Yes)

            intercept[NotImplementedError] {
              navigator.nextPage(ContactHaveYouAddedAllPage(TransactionMode), TransactionMode, userAnswers)
            }
          }
        }

        "there was no current answer" - {
          "must throw an exception" in {

            val userAnswers =
              emptyUserAnswers.add(ContactHaveYouAddedAllPage(NormalMode), ContactHaveYouAddedAll.Yes)

            intercept[NotImplementedError] {
              navigator.nextPage(ContactHaveYouAddedAllPage(TransactionMode), TransactionMode, userAnswers)
            }
          }
        }

        "previously did not add second contact then chose to add another" - {
          "must redirect to second contact name page" in {

            val userAnswers =
              emptyUserAnswers
                .add(ContactHaveYouAddedAllPage(NormalMode), ContactHaveYouAddedAll.No)
                .add(ContactHaveYouAddedAllPage(TransactionMode), ContactHaveYouAddedAll.Yes)

            navigator.nextPage(
              ContactHaveYouAddedAllPage(TransactionMode),
              TransactionMode,
              userAnswers
            ) mustBe routes.ContactNameController.onPageLoad(Second, TransactionMode)
          }
        }

        "some other combination of previous and current answers" - {
          "must redirect to contact check your answers page" in {

            val userAnswers =
              emptyUserAnswers
                .add(ContactHaveYouAddedAllPage(NormalMode), ContactHaveYouAddedAll.Yes)
                .add(ContactHaveYouAddedAllPage(TransactionMode), ContactHaveYouAddedAll.No)

            navigator.nextPage(
              ContactHaveYouAddedAllPage(TransactionMode),
              TransactionMode,
              userAnswers
            ) mustBe routes.ContactCheckYourAnswersController.onPageLoad()
          }
        }
      }

      "on second ContactNamePage" - {
        "must redirect to second ContactEmailPage" in {
          navigator.nextPage(
            ContactNamePage(Second, TransactionMode),
            TransactionMode,
            emptyUserAnswers
          ) mustBe routes.ContactEmailController.onPageLoad(Second, TransactionMode)
        }
      }

      "on second ContactEmailPage" - {
        "must redirect to contact check your answers page" in {
          navigator.nextPage(
            ContactEmailPage(Second, TransactionMode),
            TransactionMode,
            emptyUserAnswers
          ) mustBe routes.ContactCheckYourAnswersController.onPageLoad()
        }
      }
    }
  }
}
