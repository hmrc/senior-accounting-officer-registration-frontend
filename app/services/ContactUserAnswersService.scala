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

package services

import models.*
import models.Area.*
import models.ContactType.*
import pages.*
import play.api.Logging
import play.api.libs.json.*
import play.api.libs.json.Reads.*

import javax.inject.Inject

class ContactUserAnswersService @Inject extends Logging {

  def sanitise(userAnswers: UserAnswers): UserAnswers = {
    userAnswers.get(ContactHaveYouAddedAllPage(NormalMode)) match {
      case Some(ContactHaveYouAddedAll.Yes) =>
        userAnswers
          .clearTransactionArea()
          .copyCommittedAreaToTransactionArea()
      case Some(ContactHaveYouAddedAll.No) =>
        userAnswers
          .clearTransactionArea()
          .clearCommittedAreaSecondContact()
          .copyCommittedAreaToTransactionArea()
      case None => ???
    }
  }

  extension (userAnswers: UserAnswers) {
    private def copyCommittedAreaToTransactionArea(): UserAnswers = {
      userAnswers.transformUserAnswers(
        (__).json.update(
          __.read[JsObject].map { _ =>
            Json.obj(TRANSACTION_PATH -> (userAnswers.data \ COMMITTED_PATH).getOrElse(???))
          }
        )
      )
    }

    private def clearCommittedAreaSecondContact(): UserAnswers = {
      userAnswers.transformUserAnswers(
        (__ \ COMMITTED_PATH \ SECOND_CONTACT_PATH).json.prune
      )
    }

    private def clearTransactionArea(): UserAnswers = {
      userAnswers.transformUserAnswers((__ \ TRANSACTION_PATH).json.prune)
    }

    private def transformUserAnswers(transformer: Reads[JsObject]): UserAnswers = {
      userAnswers.data.transform(transformer) match {
        case JsError(error) => {
          logger.error("Json transformation error: " + error)
          ???
        }
        case JsSuccess(updatedData, _) => userAnswers.copy(data = updatedData)
      }
    }
  }

  def commitTransaction(userAnswers: UserAnswers): UserAnswers = {
    userAnswers.get(ContactHaveYouAddedAllPage(TransactionMode)) match {
      case Some(ContactHaveYouAddedAll.Yes) => commitTwoContactsTransaction(userAnswers)
      case Some(ContactHaveYouAddedAll.No)  => commitOneContactTransaction(userAnswers)
      case None                             => ???
    }
  }

  private def commitTwoContactsTransaction(userAnswers: UserAnswers): UserAnswers = {
    (for {
      decision     <- userAnswers.get(ContactHaveYouAddedAllPage(TransactionMode))
      contactName  <- userAnswers.get(ContactNamePage(Second, TransactionMode))
      contactEmail <- userAnswers.get(ContactEmailPage(Second, TransactionMode))
    } yield {
      (for {
        userAnswers <- userAnswers.set(ContactHaveYouAddedAllPage(NormalMode), decision)
        userAnswers <- userAnswers.set(ContactNamePage(Second, NormalMode), contactName)
        userAnswers <- userAnswers.set(ContactEmailPage(Second, NormalMode), contactEmail)
      } yield userAnswers).getOrElse(???)
    }).getOrElse(???)
  }

  private def commitOneContactTransaction(userAnswers: UserAnswers): UserAnswers = {
    (for {
      decision <- userAnswers.get(ContactHaveYouAddedAllPage(TransactionMode))
    } yield {
      userAnswers
        .set(ContactHaveYouAddedAllPage(NormalMode), decision)
        .getOrElse(???)
    }).getOrElse(???)
  }
}
