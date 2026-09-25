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

import config.FeatureConfigSupport
import controllers.routes
import models.*
import models.ContactType.*
import models.config.FeatureToggle.ContactFlowReshuffle
import pages.*
import play.api.Configuration
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject() (configuration: Configuration) extends FeatureConfigSupport {
  given Configuration = configuration

  private enum ContactFlow {
    case Legacy, Reshuffled
  }

  private val legacyNormalRoutes: Page => UserAnswers => Call = {
    case NominatedCompanyDetailsGuidancePage      => _ => routes.GrsController.start()
    case ContactNamePage(contactType, NormalMode) =>
      _ => routes.ContactEmailController.onPageLoad(contactType, NormalMode)
    case ContactEmailPage(First, NormalMode)  => _ => routes.ContactCheckYourAnswersController.onPageLoadLegacy(First)
    case ContactEmailPage(Second, NormalMode) => _ => routes.ContactCheckYourAnswersController.onPageLoadLegacy(Second)
    case ContactCheckYourAnswersPage(contactType) =>
      _ =>
        contactType match {
          case First =>
            routes.ContactHaveYouAddedAllController.onPageLoad(First, NormalMode)
          case Second =>
            routes.IndexController.onPageLoad()
        }
    case ContactHaveYouAddedAllPage(First, NormalMode) =>
      userAnswers =>
        if userAnswers.get(ContactHaveYouAddedAllPage(First, NormalMode)).contains(ContactHaveYouAddedAll.Yes) then {
          routes.IndexController.onPageLoad()
        } else {
          routes.ContactNameController.onPageLoad(
            Second,
            NormalMode
          )
        }
    case _ =>
      _ => {
        routes.IndexController.onPageLoad()
      }
  }

  private val legacyCheckRoutes: Page => UserAnswers => Call = {
    case ContactNamePage(contactType, NormalMode) =>
      _ => routes.ContactCheckYourAnswersController.onPageLoadLegacy(contactType)
    case ContactEmailPage(contactType, NormalMode) =>
      _ => routes.ContactCheckYourAnswersController.onPageLoadLegacy(contactType)
    case _ => _ => routes.IndexController.onPageLoad()
  }

  private val reshuffledNormalRoutes: Page => UserAnswers => Call = {
    case NominatedCompanyDetailsGuidancePage      => _ => routes.GrsController.start()
    case ContactNamePage(contactType, NormalMode) =>
      _ => routes.ContactEmailController.onPageLoad(contactType, NormalMode)
    case ContactEmailPage(First, NormalMode) =>
      _ => routes.ContactHaveYouAddedAllController.onPageLoad(First, NormalMode)
    case ContactEmailPage(Second, NormalMode) => _ => routes.ContactCheckYourAnswersController.onPageLoadReshuffled()
    case ContactsCheckYourAnswersPage         => _ => routes.IndexController.onPageLoad()
    case ContactHaveYouAddedAllPage(First, NormalMode) =>
      userAnswers =>
        if userAnswers.get(ContactHaveYouAddedAllPage(First, NormalMode)).contains(ContactHaveYouAddedAll.No) then {
          routes.ContactCheckYourAnswersController.onPageLoadReshuffled()
        } else {
          routes.ContactNameController.onPageLoad(Second, NormalMode)
        }
    case _ =>
      _ => routes.IndexController.onPageLoad()
  }

  private val reshuffledCheckRoutes: Page => UserAnswers => Call = {
    case ContactNamePage(_, _)                => _ => routes.ContactCheckYourAnswersController.onPageLoadReshuffled()
    case ContactEmailPage(_, _)               => _ => routes.ContactCheckYourAnswersController.onPageLoadReshuffled()
    case ContactHaveYouAddedAllPage(First, _) =>
      userAnswers =>
        userAnswers.get(ContactHaveYouAddedAllPage(First, NormalMode)) match {
          case Some(ContactHaveYouAddedAll.No) => routes.ContactCheckYourAnswersController.onPageLoadReshuffled()
          case Some(ContactHaveYouAddedAll.Yes)
              if userAnswers.get(ContactNamePage(Second, NormalMode)).isDefined &&
                userAnswers.get(ContactEmailPage(Second, NormalMode)).isDefined =>
            routes.ContactCheckYourAnswersController.onPageLoadReshuffled()
          case Some(ContactHaveYouAddedAll.Yes) =>
            routes.ContactNameController.onPageLoad(Second, NormalMode)
          case _ =>
            routes.IndexController.onPageLoad()
        }
    case _ => _ => routes.IndexController.onPageLoad()
  }

  private val transactionRoutes: Page => UserAnswers => Call = {
    case ContactHaveYouAddedAllPage(First, TransactionMode) =>
      userAnswers => {
        val previousAnswer = userAnswers.get(ContactHaveYouAddedAllPage(First, NormalMode))
        val currentAnswer  = userAnswers.get(ContactHaveYouAddedAllPage(First, TransactionMode))
        (previousAnswer, currentAnswer) match {
          case (Some(ContactHaveYouAddedAll.No), Some(ContactHaveYouAddedAll.Yes)) =>
            routes.ContactNameController.onPageLoad(Second, TransactionMode)
          case (Some(_), Some(_)) =>
            routes.ContactCheckYourAnswersController.onPageLoadReshuffled()
          case _ => ???
        }
      }
    case ContactNamePage(Second, TransactionMode) =>
      _ => routes.ContactEmailController.onPageLoad(Second, TransactionMode)
    case ContactEmailPage(Second, TransactionMode) =>
      _ => routes.ContactCheckYourAnswersController.onPageLoadReshuffled()
    case _ => _ => routes.IndexController.onPageLoad()
  }

  private def currentFlow: ContactFlow =
    if isEnabled(ContactFlowReshuffle) then ContactFlow.Reshuffled else ContactFlow.Legacy

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = (currentFlow, mode) match {
    case (ContactFlow.Legacy, NormalMode)          => legacyNormalRoutes(page)(userAnswers)
    case (ContactFlow.Legacy, CheckMode)           => legacyCheckRoutes(page)(userAnswers)
    case (ContactFlow.Legacy, TransactionMode)     => ???
    case (ContactFlow.Reshuffled, NormalMode)      => reshuffledNormalRoutes(page)(userAnswers)
    case (ContactFlow.Reshuffled, CheckMode)       => reshuffledCheckRoutes(page)(userAnswers)
    case (ContactFlow.Reshuffled, TransactionMode) => transactionRoutes(page)(userAnswers)
  }
}
