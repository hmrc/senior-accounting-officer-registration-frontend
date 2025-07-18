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

import models.ContactType.First
import play.api.mvc.JavascriptLiteral

enum ContactType {
  case First, Second, Third
}

object ContactType {
  extension (value: ContactType) {
    def toMongoPath: String = value match {
      case First  => "firstContact"
      case Second => "secondContact"
      case Third  => "thirdContact"
    }
    def messageKey: String = value match {
      case First  => "first"
      case Second => "second"
      case Third  => "third"
    }
  }

  given JavascriptLiteral[ContactType] = {
    case First  => "First"
    case Second => "Second"
    case Third  => "Third"
  }

  type NoneFinal = First.type | Second.type

  given JavascriptLiteral[NoneFinal] = {
    case First  => "First"
    case Second => "Second"
  }

}
