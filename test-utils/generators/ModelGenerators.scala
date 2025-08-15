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

package generators

import models.*
import org.scalacheck.{Arbitrary, Gen}

import java.time.*

trait ModelGenerators

given arbitraryContactHaveYouAddedAll: Arbitrary[ContactHaveYouAddedAll] =
  Arbitrary {
    Gen.oneOf(ContactHaveYouAddedAll.values.toSeq)
  }

val genLocalTime: Gen[LocalTime] =
  Gen
    .choose(
      min = LocalTime.MIN.toSecondOfDay.toLong,
      max = LocalTime.MAX.toSecondOfDay.toLong
    )
    .map(LocalTime.ofSecondOfDay)

val genLocalDateTime: Gen[LocalDate] =
  Gen
    .choose(
      min = LocalDate.of(0, 1, 1).toEpochDay,
      max = LocalDate.of(9999, 12, 31).toEpochDay
    )
    .map(LocalDate.ofEpochDay)

val genZonedDateTime: Gen[ZonedDateTime] =
  for {
    date <- genLocalDateTime
    time <- genLocalTime
  } yield ZonedDateTime.of(date, time, ZoneOffset.UTC)
