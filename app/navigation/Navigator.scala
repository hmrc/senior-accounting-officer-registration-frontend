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
import pages.*
import play.api.Configuration
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject() (configuration: Configuration) extends FeatureConfigSupport {
  given Configuration = configuration

  private val normalRoutes: Page => UserAnswers => Call = {
    case NominatedCompanyDetailsGuidancePage      => _ => routes.GrsController.start()
    case ContactNamePage(contactType, NormalMode) =>
      _ => routes.ContactEmailController.onPageLoad(contactType, NormalMode)
    case ContactEmailPage(First, NormalMode) =>
      _ => routes.ContactHaveYouAddedAllController.onPageLoad(NormalMode)
    case ContactEmailPage(Second, NormalMode)   => _ => routes.ContactCheckYourAnswersController.onPageLoad()
    case ContactsCheckYourAnswersPage           => _ => routes.IndexController.onPageLoad()
    case ContactHaveYouAddedAllPage(NormalMode) =>
      userAnswers =>
        if userAnswers.get(ContactHaveYouAddedAllPage(NormalMode)).contains(ContactHaveYouAddedAll.No) then {
          routes.ContactCheckYourAnswersController.onPageLoad()
        } else {
          routes.ContactNameController.onPageLoad(Second, NormalMode)
        }
    case _ =>
      _ => routes.IndexController.onPageLoad()
  }

  private val checkRoutes: Page => UserAnswers => Call = {
    case ContactNamePage(_, _)         => _ => routes.ContactCheckYourAnswersController.onPageLoad()
    case ContactEmailPage(_, _)        => _ => routes.ContactCheckYourAnswersController.onPageLoad()
    case ContactHaveYouAddedAllPage(_) =>
      userAnswers =>
        userAnswers.get(ContactHaveYouAddedAllPage(NormalMode)) match {
          case Some(ContactHaveYouAddedAll.No) => routes.ContactCheckYourAnswersController.onPageLoad()
          case Some(ContactHaveYouAddedAll.Yes)
              if userAnswers.get(ContactNamePage(Second, NormalMode)).isDefined &&
                userAnswers.get(ContactEmailPage(Second, NormalMode)).isDefined =>
            routes.ContactCheckYourAnswersController.onPageLoad()
          case Some(ContactHaveYouAddedAll.Yes) =>
            routes.ContactNameController.onPageLoad(Second, NormalMode)
          case _ =>
            routes.IndexController.onPageLoad()
        }
    case _ => _ => routes.IndexController.onPageLoad()
  }

  private val transactionRoutes: Page => UserAnswers => Call = {
    case ContactHaveYouAddedAllPage(TransactionMode) =>
      userAnswers => {
        val previousAnswer = userAnswers.get(ContactHaveYouAddedAllPage(NormalMode))
        val currentAnswer  = userAnswers.get(ContactHaveYouAddedAllPage(TransactionMode))
        (previousAnswer, currentAnswer) match {
          case (Some(ContactHaveYouAddedAll.No), Some(ContactHaveYouAddedAll.Yes)) =>
            routes.ContactNameController.onPageLoad(Second, TransactionMode)
          case (Some(_), Some(_)) =>
            routes.ContactCheckYourAnswersController.onPageLoad()
          case _ => ???
        }
      }
    case ContactNamePage(Second, TransactionMode) =>
      _ => routes.ContactEmailController.onPageLoad(Second, TransactionMode)
    case ContactEmailPage(Second, TransactionMode) =>
      _ => routes.ContactCheckYourAnswersController.onPageLoad()
    case _ => _ => routes.IndexController.onPageLoad()
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode      => normalRoutes(page)(userAnswers)
    case CheckMode       => checkRoutes(page)(userAnswers)
    case TransactionMode => transactionRoutes(page)(userAnswers)
  }
}
