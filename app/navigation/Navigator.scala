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

import controllers.routes
import models.*
import pages.*
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Call = {
    case IsIncorporatedUnderUkCompanyActsPage =>
      _.get(IsIncorporatedUnderUkCompanyActsPage) match {
        case Some(IsIncorporatedUnderUkCompanyActs.Yes) =>
          routes.IsGroupOrStandaloneController.onPageLoad()
        case Some(IsIncorporatedUnderUkCompanyActs.No) =>
          routes.ServiceNotSuitableController.onNotInUkPageLoad()
        case _ => routes.JourneyRecoveryController.onPageLoad()
      }
    case IsGroupOrStandalonePage =>
      _.get(IsGroupOrStandalonePage) match {
        case Some(IsGroupOrStandalone.Group) =>
          routes.GroupAnnualRevenuesController.onPageLoad()
        case Some(IsGroupOrStandalone.Standalone) =>
          routes.StandaloneAnnualRevenuesController.onPageLoad()
        case _ => routes.JourneyRecoveryController.onPageLoad()
      }
    case GroupAnnualRevenuesPage =>
      _.get(GroupAnnualRevenuesPage) match {
        case Some(true) =>
          routes.EligibleController.onGroupPageLoad()
        case Some(false) =>
          routes.GroupBalanceSheetController.onPageLoad()
        case _ => routes.JourneyRecoveryController.onPageLoad()
      }
    case GroupBalanceSheetPage =>
      _.get(GroupBalanceSheetPage) match {
        case Some(true) =>
          routes.EligibleController.onGroupPageLoad()
        case Some(false) =>
          routes.ServiceNotSuitableController.onGroupUnderThresholdPageLoad()
        case _ => routes.JourneyRecoveryController.onPageLoad()
      }
    case StandaloneAnnualRevenuesPage =>
      _.get(StandaloneAnnualRevenuesPage) match {
        case Some(true) =>
          routes.EligibleController.onStandalonePageLoad()
        case Some(false) =>
          routes.StandaloneBalanceSheetController.onPageLoad()
        case _ => routes.JourneyRecoveryController.onPageLoad()
      }
    case StandaloneBalanceSheetPage =>
      _.get(StandaloneBalanceSheetPage) match {
        case Some(true) =>
          routes.EligibleController.onStandalonePageLoad()
        case Some(false) =>
          routes.ServiceNotSuitableController.onStandaloneUnderThresholdPageLoad()
        case _ => routes.JourneyRecoveryController.onPageLoad()
      }
    case _ => _ => routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = { case _ =>
    _ => routes.CheckYourAnswersController.onPageLoad()
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }
}
