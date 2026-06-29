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

package models.signup

import play.api.libs.functional.syntax.*
import play.api.libs.json.{Json, JsonValidationError, OFormat, OWrites, Reads, __}

final case class SignUpRequest(idType: String, idNumber: String)

object SignUpRequest {

  private val utrRegex = "^[0-9]{10}$".r

  given Reads[SignUpRequest] = (
    (__ \ "idType").read[String].filter(JsonValidationError("error.expected.utr"))(_ == "UTR") and
      (__ \ "idNumber").read[String].filter(JsonValidationError("error.expected.10.digits"))(utrRegex.matches)
  )(SignUpRequest.apply)

  given OWrites[SignUpRequest] = Json.writes[SignUpRequest]
}

final case class EtmpSubscriptionRequest(idType: String, idNumber: String)

object EtmpSubscriptionRequest {
  given OWrites[EtmpSubscriptionRequest] = Json.writes[EtmpSubscriptionRequest]

  def fromSignUpRequest(request: SignUpRequest): EtmpSubscriptionRequest =
    EtmpSubscriptionRequest(request.idType, request.idNumber)
}

final case class EtmpSubscriptionResponse(saoSubscriptionId: String)

object EtmpSubscriptionResponse {
  given OFormat[EtmpSubscriptionResponse] = Json.format[EtmpSubscriptionResponse]
}

final case class SignUpResponse(saoSubscriptionId: String)

object SignUpResponse {
  given OWrites[SignUpResponse] = Json.writes[SignUpResponse]
}
