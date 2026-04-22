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

package models

import models.RegistrationCompleteDetailsSpec.*
import models.registration.RegistrationCompleteDetails
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.*

class RegistrationCompleteDetailsSpec
    extends AnyFreeSpec
    with Matchers
    with OptionValues
    with ScalaCheckPropertyChecks {

  "RegistrationCompleteDetails" - {
    "must deserialise valid values" in {

      val gen: Gen[String] = genValidRegistrationId

      forAll(gen) { registrationId =>
        val jsValue: JsValue = Json.obj(
          "registrationId" -> registrationId
        )
        jsValue
          .validate[RegistrationCompleteDetails]
          .asOpt
          .value mustEqual RegistrationCompleteDetails(
          registrationId = registrationId
        )
      }
    }

    "must fail to deserialise invalid values" in {

      val gen: Gen[JsValue] = Gen.frequency(
        1 -> genInvalidJsObj,
        1 -> genMissingRegistrationId
      )

      forAll(gen) { invalidJsValue =>
        invalidJsValue.validate[RegistrationCompleteDetails] mustBe an[JsError]
      }
    }

    "must serialise" in {

      val gen: Gen[String] = genValidRegistrationId

      forAll(gen) { registrationId =>
        val registrationCompleteDetails: RegistrationCompleteDetails = RegistrationCompleteDetails(
          registrationId = registrationId
        )

        Json.toJson(registrationCompleteDetails) mustEqual Json.obj(
          "registrationId" -> registrationId
        )
      }
    }
  }
}

object RegistrationCompleteDetailsSpec {

  val genValidRegistrationId: Gen[String] = Gen.alphaNumStr.suchThat(_.nonEmpty)

  val genInvalidJsObj: Gen[JsValue] = Gen.alphaNumStr.map(JsString.apply)

  val genMissingRegistrationId: Gen[JsValue] = Json.obj()

}
