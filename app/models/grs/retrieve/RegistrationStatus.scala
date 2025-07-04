/*
 * Copyright 2024 HM Revenue & Customs
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

package models.grs.retrieve

import play.api.libs.json.*

enum RegistrationStatus {
  case REGISTERED, REGISTRATION_FAILED, REGISTRATION_NOT_CALLED
}

object RegistrationStatus {

  val RegisteredKey            = "REGISTERED"
  val RegistrationFailedKey    = "REGISTRATION_FAILED"
  val RegistrationNotCalledKey = "REGISTRATION_NOT_CALLED"

  given Reads[RegistrationStatus] = (json: JsValue) =>
    json.validate[String].collect(JsonValidationError(s"Invalid registration status $json")) {
      case RegisteredKey            => REGISTERED
      case RegistrationFailedKey    => REGISTRATION_FAILED
      case RegistrationNotCalledKey => REGISTRATION_NOT_CALLED
    }
}

sealed trait Registration

object Registration {
  final case class Registered(
      registeredBusinessPartnerId: String
  ) extends Registration

  final case class RegistrationFailed(
      registrationFailures: List[Failure]
  ) extends Registration

  case object RegistrationNotCalled extends Registration

  val registrationStatusKey          = "registrationStatus"
  val registeredBusinessPartnerIdKey = "registeredBusinessPartnerId"
  val registrationFailuresKey        = "failures"

  given Reads[Registration] = (json: JsValue) =>
    (json \ registrationStatusKey)
      .validate[RegistrationStatus]
      .collect(JsonValidationError(s"Invalid registration")) {
        case RegistrationStatus.REGISTERED =>
          (json \ registeredBusinessPartnerIdKey)
            .validate[String]
            .collect(JsonValidationError("Invalid REGISTERED")) { case businessPartnerId: String =>
              Registered(businessPartnerId)
            }
            .get
        case RegistrationStatus.REGISTRATION_FAILED =>
          (json \ registrationFailuresKey)
            .validateOpt[List[Failure]]
            .collect(JsonValidationError("Invalid REGISTRATION_FAILED")) {
              case Some(failures) => RegistrationFailed(failures)
              case _              => RegistrationFailed(List.empty)
            }
            .get
        case RegistrationStatus.REGISTRATION_NOT_CALLED =>
          RegistrationNotCalled
      }

}

final case class Failure(code: String, reason: String)

object Failure {
  given Reads[Failure] = Json.reads[Failure]
}
