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

package services

import base.SpecBase
import models.registration.CompanyDetails
import models.{ContactInfo, DashboardStage}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages.{CompanyDetailsPage, ContactsPage}

class DashboardServiceSpec extends SpecBase with GuiceOneAppPerSuite {
  val SUT: DashboardService              = app.injector.instanceOf[DashboardService]
  val testCompanyDetails: CompanyDetails = CompanyDetails("", "", "", "")
  val testContactInfo: List[ContactInfo] = Nil

  "DashboardService.deriveCurrentStage when" - {
    "there are no userAnswers must return CompanyDetails" in {
      SUT.deriveCurrentStage(None) mustBe DashboardStage.CompanyDetails
    }

    "userAnswers has no companyDetails must return CompanyDetails" in {
      SUT.deriveCurrentStage(Some(emptyUserAnswers)) mustBe DashboardStage.CompanyDetails
    }

    "userAnswers has companyDetails but not contactInfo must return ContactsInfo" in {
      SUT.deriveCurrentStage(
        emptyUserAnswers.set(CompanyDetailsPage, testCompanyDetails).toOption
      ) mustBe DashboardStage.ContactsInfo
    }

    "userAnswers has companyDetails & contactInfo must return Submission" in {
      SUT.deriveCurrentStage(
        emptyUserAnswers
          .set(CompanyDetailsPage, testCompanyDetails)
          .get
          .set(ContactsPage, testContactInfo)
          .toOption
      ) mustBe DashboardStage.Submission
    }
  }

}
