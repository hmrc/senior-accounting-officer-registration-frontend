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

package models.registration

import play.api.libs.functional.syntax.{toApplicativeOps, toFunctionalBuilderOps, unlift}
import play.api.libs.json.Reads.*
import play.api.libs.json.{Format, JsError, JsPath, JsString, JsSuccess, Json, OFormat, Reads, Writes}

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}

final case class RegistrationCompleteDetails(
    companyName: String,
    registrationId: String,
    registrationDateTime: LocalDateTime
) {
  // helper method
  def formattedDateTime: String = {
    registrationDateTime.format(RegistrationCompleteDetails.customDateFormatter)
  }
}

object RegistrationCompleteDetails {

  val customDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy 'at' h.mma '(GMT)'")

  given localDateTimeFormat: Format[LocalDateTime] = Format(
    Reads[LocalDateTime] { json =>
      json.validate[String].flatMap { dateStr =>
        try {
          JsSuccess(LocalDateTime.parse(dateStr, customDateFormatter))
        } catch {
          case _: Exception =>
            try {
              JsSuccess(LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            } catch {
              case e: Exception => JsError(s"Invalid date time format r: ${e.getMessage}")
            }
        }
      }
    },
    Writes[LocalDateTime] { dateTime =>
      JsString(dateTime.format(customDateFormatter))
    }
  )

  given registrationReads: Reads[RegistrationCompleteDetails] = (
    (JsPath \ "companyName").read[String](minLength[String](1) keepAnd maxLength[String](255)) and
      (JsPath \ "registrationId").read[String](minLength[String](1) keepAnd maxLength[String](255)) and
      (JsPath \ "registrationDateTime").read[LocalDateTime]
  )(RegistrationCompleteDetails.apply _)

  given registrationWrites: Writes[RegistrationCompleteDetails] = (
    (JsPath \ "companyName").write[String] and
      (JsPath \ "registrationId").write[String] and
      (JsPath \ "registrationDateTime").write[LocalDateTime]
  )((details: RegistrationCompleteDetails) =>
    (details.companyName, details.registrationId, details.registrationDateTime)
  )

  given registrationFormat: Format[RegistrationCompleteDetails] = Format(registrationReads, registrationWrites)
}
