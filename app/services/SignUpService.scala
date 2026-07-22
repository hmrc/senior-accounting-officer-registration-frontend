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

import connectors.SignUpConnector
import models.UserAnswers
import models.registration.*
import pages.*
import play.api.libs.json.Json
import services.SignUpService.*
import services.SignUpService.SignUpResult.*
import uk.gov.hmrc.http.*

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NonFatal

import javax.inject.Inject

class SignUpService @Inject() (
    signupConnector: SignUpConnector,
    contactService: ContactCheckYourAnswersService
)(using ExecutionContext) {

  def submit(userAnswers: UserAnswers)(using HeaderCarrier): Future[SignUpResult] =
    mapRequest(userAnswers)
      .fold(Future.successful(InsufficientUserAnswers))(callSignUp)

  private def mapRequest(userAnswers: UserAnswers): Option[SignUpRequest] =
    for {
      companyDetails <- userAnswers.get(CompanyDetailsPage)
      contact        <- getContacts(userAnswers)
    } yield SignUpRequest(
      etmpSafeId = companyDetails.registeredBusinessPartnerId,
      nominatedCompany = NominatedCompany(
        name = companyDetails.companyName,
        crn = companyDetails.companyNumber,
        utr = companyDetails.ctUtr
      ),
      contacts = contact
    )

  private def getContacts(userAnswers: UserAnswers): Option[List[Contact]] =
    Some(
      contactService
        .getContacts(userAnswers)
        .map(info => Contact(name = info.name, email = info.email, status = "valid", language = "en-GB"))
    ).filter(_.nonEmpty)

  private def callSignUp(request: SignUpRequest)(using HeaderCarrier) =
    signupConnector.submit(request).map {
      case HttpResponse(200, body, _) =>
        Try(Json.parse(body).as[SignUpResponse]).fold(
          {
            case NonFatal(_) => MalformedResponse
            case fatal       => throw fatal
          },
          response => SignUpResult.Success(response.subscriptionId)
        )
      case HttpResponse(400, _, _)                  => BadRequestFailure
      case HttpResponse(status @ (500 | 502), _, _) => ProtectedServiceFailure(status)
      case HttpResponse(status, _, _)               => UnknownFailure(status)
    }
}

object SignUpService {

  sealed trait Failure

  enum SignUpResult {
    case Success(subscriptionId: String)      extends SignUpResult
    case InsufficientUserAnswers              extends SignUpResult with Failure
    case MalformedResponse                    extends SignUpResult with Failure
    case BadRequestFailure                    extends SignUpResult with Failure
    case ProtectedServiceFailure(status: Int) extends SignUpResult with Failure
    case UnknownFailure(status: Int)          extends SignUpResult with Failure
  }
}
