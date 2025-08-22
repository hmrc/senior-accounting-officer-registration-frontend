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

import generators.genZonedDateTime
import models.RegistrationCompleteDetailsSpec.*
import models.registration.RegistrationCompleteDetails
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.*

import scala.util.Random

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_DATE_TIME

class RegistrationCompleteDetailsSpec
    extends AnyFreeSpec
    with Matchers
    with OptionValues
    with ScalaCheckPropertyChecks {

  private val app: Application =
    new GuiceApplicationBuilder()
      .configure(
        Map(
          "play.i18n.langs.0" -> "en",
          "play.i18n.langs.1" -> "cy"
        )
      )
      .build()
  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  "RegistrationCompleteDetails" - {
    "must deserialise valid values" in {

      val gen: Gen[(String, String, ZonedDateTime)] = for {
        companyName          <- genValidCompanyName
        registrationId       <- genValidRegistrationId
        registrationDateTime <- genZonedDateTime
      } yield (companyName, registrationId, registrationDateTime)

      forAll(gen) { (companyName, registrationId, registrationDateTime) =>

        val companyDetailsJsValue: JsValue = Json.parse(s"""
                          | {
                          |   "companyName" : "$companyName",
                          |   "registrationId" : "$registrationId",
                          |   "registrationDateTime" : "$registrationDateTime"
                          | }
                          |""".stripMargin)
        companyDetailsJsValue
          .validate[RegistrationCompleteDetails]
          .asOpt
          .value mustEqual RegistrationCompleteDetails(
          companyName = companyName,
          registrationId = registrationId,
          registrationDateTime = registrationDateTime
        )
      }
    }

    "must fail to deserialise invalid values" in {

      val gen: Gen[JsValue] = Gen.frequency(
        1  -> genInvalidJsObj,
        1  -> genMissingCompanyName,
        1  -> genMissingRegistrationDateTime,
        1  -> genMissingRegistrationId,
        10 -> genInvalidRegistrationDateTime
      )

      forAll(gen) { invalidCompanyDetailsJsValue =>
        invalidCompanyDetailsJsValue.validate[RegistrationCompleteDetails] mustBe an[JsError]
      }
    }

    "must serialise" in {

      val gen: Gen[(String, String, ZonedDateTime)] = for {
        companyName          <- genValidCompanyName
        registrationId       <- genValidRegistrationId
        registrationDateTime <- genZonedDateTime
      } yield (companyName, registrationId, registrationDateTime)

      forAll(gen) { (companyName, registrationId, registrationDateTime) =>
        val registrationCompleteDetails: RegistrationCompleteDetails = RegistrationCompleteDetails(
          companyName = companyName,
          registrationId = registrationId,
          registrationDateTime = registrationDateTime
        )

        Json.toJson(registrationCompleteDetails) mustEqual Json.parse(s"""
                                                                         | {
                                                                         |   "companyName" : "$companyName",
                                                                         |   "registrationId" : "$registrationId",
                                                                         |   "registrationDateTime" : "${ISO_DATE_TIME
                                                                          .format(registrationDateTime)}"
                                                                         | }
                                                                         |""".stripMargin)
      }
    }

    "The formattedDatetimeHelperMethod" - {
      def testRegistrationCompleteDetails(registrationDateTime: ZonedDateTime): RegistrationCompleteDetails =
        RegistrationCompleteDetails(
          companyName = Random.alphanumeric.take(10).mkString,
          registrationId = Random.alphanumeric.take(10).mkString,
          registrationDateTime = registrationDateTime
        )

      "when the request language is English" - {
        val messages: Messages = messagesApi.preferred(Seq(Lang("en")))

        "must return the expected formatted date time when the date is in GMT" in {
          val testDateTime: ZonedDateTime = ZonedDateTime.parse("2025-01-01T11:45:00Z")

          testRegistrationCompleteDetails(registrationDateTime = testDateTime).formattedDateTime(
            messages
          ) mustBe "1 January 2025 at 11:45am (GMT)."
        }

        "must return the expected formatted date time when the date is in BST" in {
          val testDateTime: ZonedDateTime = ZonedDateTime.parse("2025-06-17T11:45:00Z")

          testRegistrationCompleteDetails(registrationDateTime = testDateTime).formattedDateTime(
            messages
          ) mustBe "17 June 2025 at 12:45pm (BST)."
        }

        "must not show leading 0 in date and time" in {
          val testDateTime: ZonedDateTime = ZonedDateTime.parse("2025-01-01T01:45:00Z")

          testRegistrationCompleteDetails(registrationDateTime = testDateTime).formattedDateTime(
            messages
          ) mustBe "1 January 2025 at 1:45am (GMT)."
        }
      }

      "when the request language is Welsh" - {
        val messages: Messages = messagesApi.preferred(Seq(Lang("cy")))

        "must return the expected formatted date time when the date is in GMT" in {
          val testDateTime: ZonedDateTime = ZonedDateTime.parse("2025-01-01T11:45:00Z")

          testRegistrationCompleteDetails(registrationDateTime = testDateTime).formattedDateTime(
            messages
          ) mustBe "1 Ionawr 2025 11:45yb (GMT)."
        }

        "must return the expected formatted date time when the date is in BST" in {
          val testDateTime: ZonedDateTime = ZonedDateTime.parse("2025-06-17T11:45:00Z")

          testRegistrationCompleteDetails(registrationDateTime = testDateTime).formattedDateTime(
            messages
          ) mustBe "17 Mehefin 2025 12:45yh (BST)."
        }

        "must not show leading 0 in date and time" in {
          val testDateTime: ZonedDateTime = ZonedDateTime.parse("2025-01-01T01:45:00Z")

          testRegistrationCompleteDetails(registrationDateTime = testDateTime).formattedDateTime(
            messages
          ) mustBe "1 Ionawr 2025 1:45yb (GMT)."
        }
      }

    }
  }
}

object RegistrationCompleteDetailsSpec {

  val genValidCompanyName: Gen[String]    = Gen.alphaNumStr.suchThat(_.nonEmpty)
  val genValidRegistrationId: Gen[String] = Gen.alphaNumStr.suchThat(_.nonEmpty)

  val genInvalidJsObj: Gen[JsValue] = Gen.alphaNumStr.map(JsString.apply)

  val genMissingCompanyName: Gen[JsValue] = for {
    registrationId       <- genValidRegistrationId
    registrationDateTime <- genZonedDateTime
  } yield Json.parse(s"""
       | {
       |   "registrationId" : "$registrationId",
       |   "registrationDateTime" : "$registrationDateTime"
       | }
       |""".stripMargin)

  val genMissingRegistrationDateTime: Gen[JsValue] = for {
    companyName    <- genValidCompanyName
    registrationId <- genValidRegistrationId
  } yield Json.parse(s"""
       | {
       |   "companyName" : "$companyName",
       |   "registrationId" : "$registrationId"
       | }
       |""".stripMargin)

  val genInvalidRegistrationDateTime: Gen[JsValue] = for {
    companyName          <- genValidCompanyName
    registrationId       <- genValidRegistrationId
    registrationDateTime <- Gen.alphaNumStr.suchThat(_.nonEmpty)
  } yield Json.parse(s"""
       | {
       |   "companyName" : "$companyName",
       |   "registrationId" : "$registrationId",
       |   "registrationDateTime" : "$registrationDateTime"
       | }
       |""".stripMargin)

  val genMissingRegistrationId: Gen[JsValue] = for {
    companyName          <- genValidCompanyName
    registrationDateTime <- genZonedDateTime
  } yield Json.parse(s"""
       | {
       |   "companyName" : "$companyName",
       |   "registrationDateTime" : "$registrationDateTime"
       | }
       |""".stripMargin)

}
