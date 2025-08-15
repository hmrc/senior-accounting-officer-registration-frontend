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

import play.api.i18n.Messages
import play.api.libs.json.Reads.*
import play.api.libs.json.{Json, OFormat, OWrites}

import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}
import java.util.Locale

final case class RegistrationCompleteDetails(
    companyName: String,
    registrationId: String,
    registrationDateTime: ZonedDateTime
)

object RegistrationCompleteDetails {
  given reads: OFormat[RegistrationCompleteDetails] = Json.format[RegistrationCompleteDetails]

  private def delimiter = "/"

  private def registrationDateTimeFormatter = {
    val zoneIdUk: ZoneId = ZoneId.of("Europe/London")
    val dateFormat       = "d MMMM yyyy"
    val time12HourFormat = "h:mma"
    val timeZone         = "z"
    DateTimeFormatter
      .ofPattern(
        List(dateFormat, time12HourFormat, timeZone).mkString(s"'$delimiter'")
      )
      .withZone(zoneIdUk)
  }

  extension (details: RegistrationCompleteDetails) {
    def formattedDateTime(messages: Messages): String = {
      val locale                      = Locale.forLanguageTag(messages.lang.code)
      val Array(date, time, timeZone) =
        details.registrationDateTime
          .format(registrationDateTimeFormatter.withLocale(locale))
          .split(delimiter)

      messages("registration-complete.dateTime", date, time.toLowerCase, timeZone)
    }
  }
}
