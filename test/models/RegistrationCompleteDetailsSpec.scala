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
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.i18n.{Lang, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json

import java.time.LocalDateTime

class RegistrationCompleteDetailsSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks {

  def fakeApp() = new GuiceApplicationBuilder()
    .configure(
      Map(
        "play.i8n.langs"        -> Seq("en"),
        "play.i8n.translations" -> Map(
          "en" -> Map(
            "registration-complete.dateStr1" -> "{0} at {1} (GMT)."
          )
        )
      )
    )
    .build()

  private val testDateTime = LocalDateTime.of(2025, 1, 17, 11, 45, 0)

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
      val app                           = fakeApp()
      lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
      implicit lazy val messages        = messagesApi.preferred(Seq(Lang("en")))
      val expectedFormat                = "17 January 2025 at 11:45am (GMT)."
      registrationCompleteDetails.formattedDateTime(messages) mustBe expectedFormat
    }

  }
}
