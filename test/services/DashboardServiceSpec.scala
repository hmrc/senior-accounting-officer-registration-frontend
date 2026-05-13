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
import models.*
import models.ContactType.{First, Second}
import models.registration.CompanyDetails
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages.*

import scala.util.Try

class DashboardServiceSpec extends SpecBase with GuiceOneAppPerSuite {
  val SUT: DashboardService              = app.injector.instanceOf[DashboardService]
  val testCompanyDetails: CompanyDetails = CompanyDetails("", "", "", "")
  val testContactInfo: ContactInfo       = ContactInfo("", "")
  val firstNameOnly: Try[UserAnswers]    =
    emptyUserAnswers.set(CompanyDetailsPage, testCompanyDetails).get.set(ContactNamePage(First), "testName")
  val firstNameAndEmailOnly: Try[UserAnswers] = firstNameOnly.get.set(ContactEmailPage(First), "testname@testemail.com")
  val firstContactComplete: Try[UserAnswers]  =
    firstNameAndEmailOnly.get.set(ContactHaveYouAddedAllPage(First), ContactHaveYouAddedAll.Yes)
  val firstContactAndSecondName: Try[UserAnswers] = firstNameAndEmailOnly.get
    .set(ContactHaveYouAddedAllPage(First), ContactHaveYouAddedAll.No)
    .get
    .set(ContactNamePage(Second), "testName2")
  val firstAndSecondContactComplete: Try[UserAnswers] =
    firstContactAndSecondName.get.set(ContactEmailPage(Second), "testname2@testemail.com")

  "DashboardService.deriveCurrentStage when" - {
    "there are no userAnswers must return CompanyDetails" in {
      SUT.deriveCurrentStage(None) mustBe DashboardStage.CompanyDetails
    }

    "userAnswers has no companyDetails must return CompanyDetails" in {
      SUT.deriveCurrentStage(Some(emptyUserAnswers)) mustBe DashboardStage.CompanyDetails
    }

    "userAnswers has companyDetails" - {

      "but not contactInfo must return ContactsInfo" in {
        SUT.deriveCurrentStage(
          emptyUserAnswers.set(CompanyDetailsPage, testCompanyDetails).toOption
        ) mustBe DashboardStage.ContactsInfo
      }

      "and only first name is entered, must return ContactsInfo" in {
        SUT.deriveCurrentStage(firstNameOnly.toOption) mustBe DashboardStage.ContactsInfo
      }

      "and only first name and email are entered, must return ContactsInfo" in {
        SUT.deriveCurrentStage(firstNameAndEmailOnly.toOption) mustBe DashboardStage.ContactsInfo
      }

      "and first name and email are entered, yes is selected on 'ContactHaveYouAddedAll', must return Submission" in {
        SUT.deriveCurrentStage(firstContactComplete.toOption) mustBe DashboardStage.Submission
      }

      "and completed first contact details, no is selected on 'ContactHaveYouAddedAll', only a second name is entered, must return ContactsInfo" in {
        SUT.deriveCurrentStage(firstContactAndSecondName.toOption) mustBe DashboardStage.ContactsInfo
      }

      "and completed first contact details, no is selected on 'ContactHaveYouAddedAll', a second name and email are entered, must return Submission" in {
        SUT.deriveCurrentStage(firstAndSecondContactComplete.toOption) mustBe DashboardStage.Submission
      }
    }
  }
}
