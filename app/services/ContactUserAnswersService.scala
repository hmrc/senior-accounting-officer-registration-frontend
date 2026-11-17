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

import scala.util.{Failure, Success}

import javax.inject.Inject

class ContactUserAnswersService @Inject extends Logging {

  private val notFoundException = new IllegalStateException("ContactHaveYouAddedAll not found")

  def sanitise(userAnswers: UserAnswers): UserAnswers = {
    userAnswers.get(ContactHaveYouAddedAllPage(First, NormalMode)) match {
      case Some(ContactHaveYouAddedAll.Yes) =>
        userAnswers
          .clearTransactionArea()
          .copyCommittedAreaToTransactionArea()
      case Some(ContactHaveYouAddedAll.No) =>
        userAnswers
          .clearTransactionArea()
          .clearCommittedAreaSecondContact()
          .copyCommittedAreaToTransactionArea()
      case None => throw notFoundException
    }
  }

  extension (userAnswers: UserAnswers) {
    private def copyCommittedAreaToTransactionArea(): UserAnswers = {
      userAnswers.transformUserAnswers(
        (__).json.update(
          (__ \ COMMITTED_PATH).read[JsObject].map { commited =>
            Json.obj(TRANSACTION_PATH -> commited)
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
          throw new Exception("Json transformation error: " + error)
        }
        case JsSuccess(updatedData, _) => userAnswers.copy(data = updatedData)
      }
    }
  }

  def commitTransaction(userAnswers: UserAnswers): UserAnswers = {
    userAnswers.get(ContactHaveYouAddedAllPage(First, TransactionMode)) match {
      case Some(ContactHaveYouAddedAll.Yes) => commitTwoContactsTransaction(userAnswers)
      case Some(ContactHaveYouAddedAll.No)  => commitOneContactTransaction(userAnswers)
      case None                             => throw notFoundException
    }
  }

  private def commitTwoContactsTransaction(userAnswers: UserAnswers): UserAnswers = {
    (
      userAnswers.get(ContactHaveYouAddedAllPage(First, TransactionMode)),
      userAnswers.get(ContactNamePage(Second, TransactionMode)),
      userAnswers.get(ContactEmailPage(Second, TransactionMode))
    ) match {
      case (Some(decision), Some(saoName), Some(saoEmail)) =>
        val updatedAnswers = for {
          withDecision <- userAnswers.set(ContactHaveYouAddedAllPage(First, NormalMode), decision)
          withSaoName  <- withDecision.set(ContactNamePage(Second, NormalMode), saoName)
          withSaoEmail <- withSaoName.set(ContactEmailPage(Second, NormalMode), saoEmail)
        } yield withSaoEmail
        updatedAnswers match {
          case Success(answers) => answers
          case Failure(error)   => throw error
        }
      case (Some(decision), _, Some(saoEmail)) => throw IllegalStateException("ContactNamePage not found")
      case (Some(decision), Some(saoName), _)  => throw IllegalStateException("ContactEmailPage not found")
    }
  }

  private def commitOneContactTransaction(userAnswers: UserAnswers): UserAnswers = {
    userAnswers.get(ContactHaveYouAddedAllPage(First, TransactionMode)) match {
      case Some(decision) =>
        userAnswers.set(ContactHaveYouAddedAllPage(First, NormalMode), decision) match {
          case Success(answers) => answers
          case Failure(error)   => throw error
        }
    }
  }

  object ContactUserAnswersService {
    val saoNameKey: String  = ContactNamePage(First, NormalMode).toString
    val saoEmailKey: String = ContactEmailPage(First, NormalMode).toString
  }
}
