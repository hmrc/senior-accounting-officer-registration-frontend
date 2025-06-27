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
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {

  implicit lazy val arbitraryStandaloneBalanceSheet: Arbitrary[StandaloneBalanceSheet] =
    Arbitrary {
      Gen.oneOf(StandaloneBalanceSheet.values.toSeq)
    }

  implicit lazy val arbitraryStandaloneAnnualRevenues: Arbitrary[StandaloneAnnualRevenues] =
    Arbitrary {
      Gen.oneOf(StandaloneAnnualRevenues.values.toSeq)
    }

  implicit lazy val arbitraryGroupBalanceSheet: Arbitrary[GroupBalanceSheet] =
    Arbitrary {
      Gen.oneOf(GroupBalanceSheet.values.toSeq)
    }

  implicit lazy val arbitraryGroupAnnualRevenues: Arbitrary[GroupAnnualRevenues] =
    Arbitrary {
      Gen.oneOf(GroupAnnualRevenues.values)
    }

  implicit lazy val arbitraryIsGroupOrStandalone: Arbitrary[IsGroupOrStandalone] =
    Arbitrary {
      Gen.oneOf(IsGroupOrStandalone.values)
    }

  implicit lazy val arbitraryIsIncorporatedUnderUkCompanyActs: Arbitrary[IsIncorporatedUnderUkCompanyActs] =
    Arbitrary {
      Gen.oneOf(IsIncorporatedUnderUkCompanyActs.values)
    }

}
