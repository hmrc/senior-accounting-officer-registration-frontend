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

class DashboardService {

  def deriveCurrentStage(userAnswers: Option[UserAnswers]): DashboardStage =
    userAnswers
      .fold(DashboardStage.CompanyDetails)(answers =>
        (for {
          hasCompanyDetails <- answers
            .get(CompanyDetailsPage)
            .toRight(left = DashboardStage.CompanyDetails)
          _ <- if !contactsCompleted(answers) then Left(DashboardStage.ContactsInfo) else Right(())
        } yield DashboardStage.Submission).merge
      )

  def getContact(userAnswers: UserAnswers, contactType: ContactType): Option[(String, String)] =
    for {
      name  <- userAnswers.get(ContactNamePage(contactType))
      email <- userAnswers.get(ContactEmailPage(contactType))
    } yield (name, email)

  def contactsCompleted(userAnswers: UserAnswers): Boolean = {
    val firstContact = getContact(userAnswers, First)

    def secondContact = getContact(userAnswers, Second)

    userAnswers.get(ContactHaveYouAddedAllPage(First)) match {
      case Some(ContactHaveYouAddedAll.Yes) =>
        firstContact.foldLeft(
          userAnswers.get(ContactHaveYouAddedAllPage(First)).exists(_ == ContactHaveYouAddedAll.Yes)
        )((_, _) => true)
      case Some(ContactHaveYouAddedAll.No) =>
        firstContact.foldLeft(
          userAnswers.get(ContactHaveYouAddedAllPage(First)).exists(_ == ContactHaveYouAddedAll.Yes)
        )((_, _) => secondContact.isDefined)
      case None => false
    }
  }
}
