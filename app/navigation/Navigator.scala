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

  private val normalRoutes: Page => UserAnswers => Call = {
    case NominatedCompanyDetailsGuidancePage => _ => routes.GrsController.start()
    case ContactNamePage(contactType)        => _ => routes.ContactEmailController.onPageLoad(contactType, NormalMode)
    case ContactEmailPage(First)             =>
      _ =>
        if isEnabled(ContactFlowReshuffle) then {
          routes.ContactHaveYouAddedAllController.onPageLoad(First, NormalMode)
        } else {
          routes.ContactCheckYourAnswersController.onPageLoad(First)
        }
    case ContactEmailPage(Second) =>
      _ =>
        if isEnabled(ContactFlowReshuffle) then {
          routes.ContactCheckYourAnswersController.onPageLoadNew()
        } else {
          routes.ContactCheckYourAnswersController.onPageLoad(Second)
        }
    case ContactCheckYourAnswersPage(contactType) =>
      _ =>
        contactType match {
          case First =>
            routes.ContactHaveYouAddedAllController.onPageLoad(First, NormalMode)
          case Second =>
            routes.IndexController.onPageLoad()
        }
    case ContactsCheckYourAnswersPage      => _ => routes.IndexController.onPageLoad()
    case ContactHaveYouAddedAllPage(First) =>
      userAnswers =>
        if userAnswers.get(ContactHaveYouAddedAllPage(First)).contains(ContactHaveYouAddedAll.Yes) then {
          if isEnabled(ContactFlowReshuffle) then {
            routes.ContactCheckYourAnswersController.onPageLoadNew()
          } else {
            routes.IndexController.onPageLoad()
          }
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

  private val checkRouteMap: Page => UserAnswers => Call = {
    case ContactNamePage(contactType) =>
      _ =>
        if isEnabled(ContactFlowReshuffle) then {
          routes.ContactCheckYourAnswersController.onPageLoadNew()
        } else {
          routes.ContactCheckYourAnswersController.onPageLoad(contactType)
        }
    case ContactEmailPage(contactType) =>
      _ =>
        if isEnabled(ContactFlowReshuffle) then {
          routes.ContactCheckYourAnswersController.onPageLoadNew()
        } else {
          routes.ContactCheckYourAnswersController.onPageLoad(contactType)
        }
    case ContactHaveYouAddedAllPage(First) =>
      userAnswers =>
        if isEnabled(ContactFlowReshuffle) then {
          userAnswers.get(ContactHaveYouAddedAllPage(First)) match {
            case Some(ContactHaveYouAddedAll.Yes) => routes.ContactCheckYourAnswersController.onPageLoadNew()
            case Some(ContactHaveYouAddedAll.No)
                if userAnswers.get(ContactNamePage(Second)).isDefined &&
                  userAnswers.get(ContactEmailPage(Second)).isDefined =>
              routes.ContactCheckYourAnswersController.onPageLoadNew()
            case Some(ContactHaveYouAddedAll.No) =>
              routes.ContactNameController.onPageLoad(Second, NormalMode)
            case _ =>
              routes.IndexController.onPageLoad()
          }
        } else {
          routes.IndexController.onPageLoad()
        }
    case _ => _ => routes.IndexController.onPageLoad()
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }
}
