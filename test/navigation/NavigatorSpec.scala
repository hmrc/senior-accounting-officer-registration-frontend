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
import pages.*
import models.*
import play.api.libs.json.Json

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }

      "when user answered yes on IsIncorporatedUnderUkCompanyActsPage must go to IsGroupOrStandaloneController" in {
        navigator.nextPage(
          IsIncorporatedUnderUkCompanyActsPage,
          NormalMode,
          UserAnswers(
            "id",
            data =
              Json.obj(IsIncorporatedUnderUkCompanyActsPage.toString -> IsIncorporatedUnderUkCompanyActs.Yes.toString)
          )
        ) mustBe routes.IsGroupOrStandaloneController.onPageLoad(NormalMode)
      }

      "when user answered no on IsIncorporatedUnderUkCompanyActsPage must go to IsGroupOrStandaloneController" in {
        navigator.nextPage(
          IsIncorporatedUnderUkCompanyActsPage,
          NormalMode,
          UserAnswers(
            "id",
            data =
              Json.obj(IsIncorporatedUnderUkCompanyActsPage.toString -> IsIncorporatedUnderUkCompanyActs.No.toString)
          )
        ) mustBe routes.ServiceNotSuitableController.onPageLoad()
      }

      "when user answer is missing on IsIncorporatedUnderUkCompanyActsPage must go to JourneyRecoveryController" in {
        navigator.nextPage(
          IsIncorporatedUnderUkCompanyActsPage,
          NormalMode,
          UserAnswers("id", data = Json.obj())
        ) mustBe routes.JourneyRecoveryController.onPageLoad()
      }

      "when user answered Group on IsGroupOrStandalonePage must go to GroupAnnualRevenuesController" in {
        navigator.nextPage(
          IsGroupOrStandalonePage,
          NormalMode,
          UserAnswers(
            "id",
            data = Json.obj(IsGroupOrStandalonePage.toString -> IsGroupOrStandalone.Group.toString)
          )
        ) mustBe routes.GroupAnnualRevenuesController.onPageLoad(NormalMode)
      }

      "when user answer is missing on IsGroupOrStandalonePage must go to JourneyRecoveryController" in {
        navigator.nextPage(
          IsGroupOrStandalonePage,
          NormalMode,
          UserAnswers("id", data = Json.obj())
        ) mustBe routes.JourneyRecoveryController.onPageLoad()
      }

      "when user answered Yes on GroupAnnualRevenuesPage must go to EligibleGroupController" in {
        navigator.nextPage(
          GroupAnnualRevenuesPage,
          NormalMode,
          UserAnswers(
            "id",
            data = Json.obj(GroupAnnualRevenuesPage.toString -> GroupAnnualRevenues.Yes.toString)
          )
        ) mustBe routes.EligibleGroupController.onPageLoad()
      }

      "when user answered No on GroupAnnualRevenuesPage must go to GroupBalanceSheetController" in {
        navigator.nextPage(
          GroupAnnualRevenuesPage,
          NormalMode,
          UserAnswers(
            "id",
            data = Json.obj(GroupAnnualRevenuesPage.toString -> GroupAnnualRevenues.No.toString)
          )
        ) mustBe routes.GroupBalanceSheetController.onPageLoad(NormalMode)
      }

      "when user answer is missing on GroupAnnualRevenuesPage must go to JourneyRecoveryController" in {
        navigator.nextPage(
          IsGroupOrStandalonePage,
          NormalMode,
          UserAnswers("id", data = Json.obj())
        ) mustBe routes.JourneyRecoveryController.onPageLoad()
      }

      "when user answered Yes on GroupBalanceSheetPage must go to EligibleGroupController" in {
        navigator.nextPage(
          GroupBalanceSheetPage,
          NormalMode,
          UserAnswers(
            "id",
            data = Json.obj(GroupBalanceSheetPage.toString -> GroupBalanceSheet.Yes.toString)
          )
        ) mustBe routes.EligibleGroupController.onPageLoad()
      }

      "when user answer is missing on GroupBalanceSheetPage must go to JourneyRecoveryController" in {
        navigator.nextPage(
          GroupBalanceSheetPage,
          NormalMode,
          UserAnswers("id", data = Json.obj())
        ) mustBe routes.JourneyRecoveryController.onPageLoad()
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe routes.CheckYourAnswersController
          .onPageLoad()
      }
    }
  }
}
