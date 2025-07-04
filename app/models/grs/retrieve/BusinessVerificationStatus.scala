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

enum BusinessVerificationStatus {
  case Pass,
    Fail,
    Unchallenged,
    CtEnrolled
}

object BusinessVerificationStatus {

  val PassKey                       = "PASS"
  val FailKey                       = "FAIL"
  val UnchallengedKey               = "UNCHALLENGED"
  val CtEnrolledKey                 = "CT_ENROLLED"
  val businessVerificationStatusKey = "verificationStatus"

  given Reads[BusinessVerificationStatus] = (json: JsValue) =>
    (json \ businessVerificationStatusKey)
      .validate[String]
      .collect(JsonValidationError("Invalid business validation state")) {
        case PassKey         => Pass
        case CtEnrolledKey   => CtEnrolled
        case UnchallengedKey => Unchallenged
        case FailKey         => Fail
      }

}
