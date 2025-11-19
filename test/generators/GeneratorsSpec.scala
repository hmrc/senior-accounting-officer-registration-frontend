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

import generators.Generators.{genLongEmailAddresses, genValidEmailAddress}
import org.scalacheck.Gen
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GeneratorsSpec extends AnyWordSpec with Matchers {

  val maxLength       = 50
  val numberOfSamples = 100

  "genLongEmailAddresses" must {
    "must generate emails longer than maxEmailLength" in {
      Gen.listOfN(numberOfSamples, genLongEmailAddresses).sample.get.foreach { emailStr =>
        emailStr.length must be > maxLength
      }
    }

    "must generate emails with valid format" in {
      Gen.listOfN(numberOfSamples, genLongEmailAddresses).sample.get.foreach { emailStr =>
        emailStr must endWith("@domain.com")
        emailStr must include("@")
        emailStr.count(_ == '@') mustBe 1
      }
    }
  }

  "genValidEmailAddress" must {
    "must generate emails shorter than max length" in {
      Gen.listOfN(numberOfSamples, genValidEmailAddress).sample.get.foreach { emailStr =>
        withClue(s"Email ${emailStr}, Length: ${emailStr.length}") {
          emailStr.length must be < maxLength
        }
      }
    }

    "must generate emails with valid format" in {
      Gen.listOfN(numberOfSamples, genValidEmailAddress).sample.get.foreach { emailStr =>
        withClue(s"Testing email ${emailStr}}") {
          emailStr must include("@")
          emailStr must include(".")
          emailStr.indexOf('@') must be < emailStr.lastIndexOf('.')
        }
      }
    }
  }
}
