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

import base.SpecBase
import models.signup.SignUpRequestSpec.*
import play.api.libs.json.Json

class SignUpRequestSpec extends SpecBase {

  "SignUpRequest" - {
    "must read a valid UTR request" in {
      validSignUpRequestJson.validate[SignUpRequest].asOpt mustBe Some(
        SignUpRequest("UTR", testUtr)
      )
    }

    "must reject an idType that is not UTR" in {
      Json.obj("idType" -> "NINO", "idNumber" -> testUtr).validate[SignUpRequest].isError mustBe true
    }

    "must reject an idNumber that is not 10 digits" in {
      Json.obj("idType" -> "UTR", "idNumber" -> "12345").validate[SignUpRequest].isError mustBe true
      Json.obj("idType" -> "UTR", "idNumber" -> "123456789A").validate[SignUpRequest].isError mustBe true
    }
  }
}

object SignUpRequestSpec {
  private val testUtr                = "1234567890"
  private val validSignUpRequestJson = Json.obj(
    "idType"   -> "UTR",
    "idNumber" -> testUtr
  )
}
