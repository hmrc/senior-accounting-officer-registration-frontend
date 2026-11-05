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

import models.*
import models.ContactType.{First, Second}
import pages.*

import scala.util.Left

import javax.inject.Inject

class DashboardService @Inject() () {

  def deriveCurrentStage(userAnswers: Option[UserAnswers]): DashboardStage = {
    userAnswers
      .fold(DashboardStage.CompanyDetails)(answers =>
        (for {
          hasCompanyDetails <- answers
            .get(CompanyDetailsPage)
            .toRight(left = DashboardStage.CompanyDetails)

          _ <- if contactsCompleted(answers) then Right(()) else Left(DashboardStage.ContactsInfo)
        } yield DashboardStage.Submission).merge
      )
  }

  def getContact(userAnswers: UserAnswers, contactType: ContactType): Option[(String, String)] =
    for {
      name  <- userAnswers.get(ContactNamePage(contactType, NormalMode))
      email <- userAnswers.get(ContactEmailPage(contactType, NormalMode))
    } yield (name, email)

  def contactsCompleted(userAnswers: UserAnswers): Boolean = {
    getContact(userAnswers, First)
    def secondContact = getContact(userAnswers, Second)

    userAnswers.get(ContactHaveYouAddedAllPage(NormalMode)) match {
      case Some(ContactHaveYouAddedAll.Yes) => {
        secondContact.isDefined
      }

      case Some(ContactHaveYouAddedAll.No) => {
        true
      }
      case None => false
    }
  }
}
