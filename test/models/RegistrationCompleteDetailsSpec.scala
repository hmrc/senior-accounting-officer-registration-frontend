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
import org.mockito.Mockito.when
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.must.Matchers.mustBe
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.i18n.{Lang, Messages}
import play.api.libs.json.{JsString, Json}

import java.time.LocalDateTime
import java.util.Locale

class RegistrationCompleteDetailsSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks {
  val messages = mock[Messages]

  private val testDateTime                = LocalDateTime.of(2025, 1, 17, 11, 45, 0)
  private val registrationCompleteDetails = RegistrationCompleteDetails(
    companyName = "Test Corp Ltd",
    registrationId = "REG12345",
    registrationDateTime = testDateTime
  )
  private val registrationCompleteDetailsJsonObj = Json.obj(
    "companyName"          -> "Test Corp Ltd",
    "registrationId"       -> "REG12345",
    "registrationDateTime" -> "17 January 2025 at 11:45AM (GMT)"
  )

  "The formattedDatetimeHelperMethod" - {

    "must return the date string in the correct custom format for a standard AM time in english" in {
      val expectedFormat = "17 January 2025 at 11:45am (GMT)"
      when(messages("registration-complete.dateStr1")) thenReturn "at"
      when(messages.lang) thenReturn new Lang(Locale.forLanguageTag("en"))
      registrationCompleteDetails.formattedDateTime(messages) mustBe expectedFormat
    }

    "must return the date string in the correct custom format for a standard AM time in welsh" in {
      when(messages("registration-complete.dateStr1")) thenReturn "someWelshStringFromWelshMessages"
      when(messages.lang) thenReturn new Lang(Locale.forLanguageTag("cy"))
      registrationCompleteDetails.formattedDateTime(messages) must include("17 Ionawr 2025")
      registrationCompleteDetails.formattedDateTime(messages) must include("11:45yb (GMT)")
    }
  }
}
