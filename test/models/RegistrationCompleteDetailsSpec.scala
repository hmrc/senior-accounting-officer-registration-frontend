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

import models.registration.RegistrationCompleteDetails
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.must.Matchers.mustBe
import play.api.libs.json.{JsError, JsPath, JsResult, JsSuccess, JsValue, Json}

import java.time.LocalDateTime

class RegistrationCompleteDetailsSpec extends AnyFreeSpec with Matchers {
  private val testDateTime                = LocalDateTime.of(2025, 1, 17, 11, 45, 0)
  private val registrationCompleteDetails = RegistrationCompleteDetails(
    companyName = "Test Corp Ltd",
    registrationId = "REG12345",
    registrationDateTime = testDateTime
  )
  private val registrationCompleteDetailsJsonObj = Json.obj(
    "companyName"          -> "Test Corp Ltd",
    "registrationId"       -> "REG12345",
    "registrationDateTime" -> "17 January 2025 at 11.45AM (GMT)"
  )

  "RegistrationCompleteDetails" - {
    "when serializing to json (writing)" - {
      "must produce a json object with the date tume in the custom required format" in {
        val result = Json.toJson(registrationCompleteDetails)
        result mustBe registrationCompleteDetailsJsonObj
      }
    }

    "when deserializing to json" - {
      "must create a model when the date is the custom format" in {
        val result = registrationCompleteDetailsJsonObj.validate[RegistrationCompleteDetails]
        result.isSuccess mustBe true
        result.get mustBe registrationCompleteDetails
      }
    }

    "must create a model when the date is in the ISO format" in {
      val registrationCompleteDetailsJsonObjISO = Json.obj(
        "companyName"          -> "Test Corp Ltd",
        "registrationId"       -> "REG12345",
        "registrationDateTime" -> "2025-01-17T11:45:00"
      )
      val result = registrationCompleteDetailsJsonObjISO.validate[RegistrationCompleteDetails]
      result.isSuccess mustBe true
      result.get mustBe registrationCompleteDetails
    }

    "when validating incoming JSON" - {

      "must return a JsError if companyName is empty" in {
        val invalidJson = Json.obj(
          "companyName"          -> "",
          "registrationId"       -> "REG12345",
          "registrationDateTime" -> "2025-01-17T11:45:00"
        )

        val result: JsResult[RegistrationCompleteDetails] = invalidJson.validate[RegistrationCompleteDetails]
        result match {
          case JsSuccess(_, _) => fail("Validation should have failed for empty companyName")
          case JsError(errors) =>
            errors must have size 1
            errors.head._1 mustBe (JsPath \ "companyName")
        }
      }

      "must return a JsError if companyName is too long in" in {
        val invalidJson = Json.obj(
          "companyName"          -> "A" * 256,
          "registrationId"       -> "REG12345",
          "registrationDateTime" -> "2025-01-17T11:45:00"
        )

        val result: JsResult[RegistrationCompleteDetails] = invalidJson.validate[RegistrationCompleteDetails]
        result match {
          case JsSuccess(_, _) => fail("Validation should have failed for empty companyName")
          case JsError(errors) =>
            errors must have size 1
            errors.head._1 mustBe (JsPath \ "companyName")
        }
      }

      "must return a JsError if registrationId is empty in" in {
        val invalidJson = Json.obj(
          "companyName"          -> "Test Corp Ltd",
          "registrationId"       -> "",
          "registrationDateTime" -> "17 January 2025 at 11.45AM (GMT)"
        )

        val result: JsResult[RegistrationCompleteDetails] = invalidJson.validate[RegistrationCompleteDetails]
        result match {
          case JsSuccess(_, _) => fail("Validation should have failed for empty companyName")
          case JsError(errors) =>
            errors must have size 1
            errors.head._1 mustBe (JsPath \ "registrationId")
        }
      }

      "must return a JsError if the date format is invalid in" in {
        val invalidJson = Json.obj(
          "companyName"          -> "Test Corp Ltd",
          "registrationId"       -> "REG12345",
          "registrationDateTime" -> "17-01-2025 11:45"
        )

        val result: JsResult[RegistrationCompleteDetails] = invalidJson.validate[RegistrationCompleteDetails]
        result match {
          case JsSuccess(_, _) => fail("Validation should have failed for empty companyName")
          case JsError(errors) =>
            errors must have size 1
            errors.head._1 mustBe (JsPath \ "registrationDateTime")
        }
      }

    }

    "The formattedDatetimeHelperMethod" - {

      "must return the date string in the correct custom format for a standard AM time" in {
        val expectedFormat = "17 January 2025 at 11.45AM (GMT)"
        registrationCompleteDetails.formattedDateTime mustBe expectedFormat
      }
    }

  }

}
