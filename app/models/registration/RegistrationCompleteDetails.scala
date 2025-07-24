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
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

final case class RegistrationCompleteDetails(
    companyName: String,
    registrationId: String,
    registrationDateTime: LocalDateTime
)

object RegistrationCompleteDetails {
  private val customDateFormatter1           = DateTimeFormatter.ofPattern("d MMMM yyyy")
  private val customDateFormatter2           = DateTimeFormatter.ofPattern("h.mma '(GMT)'")
  given OFormat[RegistrationCompleteDetails] = Json.format[RegistrationCompleteDetails]

  extension (details: RegistrationCompleteDetails) {
    def formattedDateTime(messages: Messages): String = {
      val locale = Locale.forLanguageTag(messages.lang.code)
      details.registrationDateTime.format(
        customDateFormatter1.withLocale(locale)
      ) + s" ${messages("registration-complete.dateStr1")} " +
        details.registrationDateTime
          .format(customDateFormatter2.withLocale(locale))
          .replace("AM", "am")
          .replace("PM", "pm")
    }
  }
}
