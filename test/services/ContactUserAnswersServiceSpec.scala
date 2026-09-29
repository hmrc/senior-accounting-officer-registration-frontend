/*
 * Copyright 2026 HM Revenue & Customs
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

package services

import base.SpecBase
import models.ContactHaveYouAddedAll
import models.NormalMode
import pages.*
import models.ContactType
import models.TransactionMode

class ContactUserAnswersServiceSpec extends SpecBase {

  def SUT = new ContactUserAnswersService

  "sanitise" - {
    "user has added one contact" in {
      val input =
        emptyUserAnswers
          .add(ContactNamePage(ContactType.First, TransactionMode), "z")
          .add(ContactEmailPage(ContactType.First, TransactionMode), "y")
          .add(ContactHaveYouAddedAllPage(ContactType.First, TransactionMode), ContactHaveYouAddedAll.Yes)
          .add(ContactNamePage(ContactType.Second, TransactionMode), "x")
          .add(ContactEmailPage(ContactType.Second, TransactionMode), "w")
          .add(ContactNamePage(ContactType.First, NormalMode), "a")
          .add(ContactEmailPage(ContactType.First, NormalMode), "b")
          .add(ContactHaveYouAddedAllPage(ContactType.First, NormalMode), ContactHaveYouAddedAll.No)
          .add(ContactNamePage(ContactType.Second, NormalMode), "c")
          .add(ContactEmailPage(ContactType.Second, NormalMode), "d")

      val expected = emptyUserAnswers
        .add(ContactNamePage(ContactType.First, NormalMode), "a")
        .add(ContactEmailPage(ContactType.First, NormalMode), "b")
        .add(ContactHaveYouAddedAllPage(ContactType.First, NormalMode), ContactHaveYouAddedAll.No)
        .add(ContactNamePage(ContactType.First, TransactionMode), "a")
        .add(ContactEmailPage(ContactType.First, TransactionMode), "b")
        .add(ContactHaveYouAddedAllPage(ContactType.First, TransactionMode), ContactHaveYouAddedAll.No)

      val result = SUT.sanitise(input)

      result.data mustBe expected.data
    }

    "user has added two contacts" in {
      val input =
        emptyUserAnswers
          .add(ContactNamePage(ContactType.First, TransactionMode), "z")
          .add(ContactEmailPage(ContactType.First, TransactionMode), "y")
          .add(ContactHaveYouAddedAllPage(ContactType.First, TransactionMode), ContactHaveYouAddedAll.Yes)
          .add(ContactNamePage(ContactType.Second, TransactionMode), "x")
          .add(ContactEmailPage(ContactType.Second, TransactionMode), "w")
          .add(ContactNamePage(ContactType.First, NormalMode), "a")
          .add(ContactEmailPage(ContactType.First, NormalMode), "b")
          .add(ContactHaveYouAddedAllPage(ContactType.First, NormalMode), ContactHaveYouAddedAll.Yes)
          .add(ContactNamePage(ContactType.Second, NormalMode), "c")
          .add(ContactEmailPage(ContactType.Second, NormalMode), "d")

      val expected = emptyUserAnswers
        .add(ContactNamePage(ContactType.First, NormalMode), "a")
        .add(ContactEmailPage(ContactType.First, NormalMode), "b")
        .add(ContactHaveYouAddedAllPage(ContactType.First, NormalMode), ContactHaveYouAddedAll.Yes)
        .add(ContactNamePage(ContactType.Second, NormalMode), "c")
        .add(ContactEmailPage(ContactType.Second, NormalMode), "d")
        .add(ContactNamePage(ContactType.First, TransactionMode), "a")
        .add(ContactEmailPage(ContactType.First, TransactionMode), "b")
        .add(ContactHaveYouAddedAllPage(ContactType.First, TransactionMode), ContactHaveYouAddedAll.Yes)
        .add(ContactNamePage(ContactType.Second, TransactionMode), "c")
        .add(ContactEmailPage(ContactType.Second, TransactionMode), "d")

      val result = SUT.sanitise(input)

      result.data mustBe expected.data
    }
  }

  "commitTransaction" - {
    "one contact added" in {
      val input =
        emptyUserAnswers
          .add(ContactHaveYouAddedAllPage(ContactType.First, TransactionMode), ContactHaveYouAddedAll.No)
          .add(ContactNamePage(ContactType.Second, TransactionMode), "z")
          .add(ContactEmailPage(ContactType.Second, TransactionMode), "y")
          .add(ContactHaveYouAddedAllPage(ContactType.First, NormalMode), ContactHaveYouAddedAll.Yes)

      val expected = emptyUserAnswers
        .add(ContactHaveYouAddedAllPage(ContactType.First, TransactionMode), ContactHaveYouAddedAll.No)
        .add(ContactNamePage(ContactType.Second, TransactionMode), "z")
        .add(ContactEmailPage(ContactType.Second, TransactionMode), "y")
        .add(ContactHaveYouAddedAllPage(ContactType.First, NormalMode), ContactHaveYouAddedAll.No)

      val result = SUT.commitTransaction(input)

      result.data mustBe expected.data
    }

    "two contacts added" in {
      val input =
        emptyUserAnswers
          .add(ContactHaveYouAddedAllPage(ContactType.First, TransactionMode), ContactHaveYouAddedAll.Yes)
          .add(ContactNamePage(ContactType.Second, TransactionMode), "a")
          .add(ContactEmailPage(ContactType.Second, TransactionMode), "b")
          .add(ContactHaveYouAddedAllPage(ContactType.First, NormalMode), ContactHaveYouAddedAll.No)
          .add(ContactNamePage(ContactType.Second, NormalMode), "z")
          .add(ContactEmailPage(ContactType.Second, NormalMode), "y")

      val expected = emptyUserAnswers
        .add(ContactHaveYouAddedAllPage(ContactType.First, TransactionMode), ContactHaveYouAddedAll.Yes)
        .add(ContactNamePage(ContactType.Second, TransactionMode), "a")
        .add(ContactEmailPage(ContactType.Second, TransactionMode), "b")
        .add(ContactHaveYouAddedAllPage(ContactType.First, NormalMode), ContactHaveYouAddedAll.Yes)
        .add(ContactNamePage(ContactType.Second, NormalMode), "a")
        .add(ContactEmailPage(ContactType.Second, NormalMode), "b")

      val result = SUT.commitTransaction(input)

      result.data mustBe expected.data
    }

    "missing ContactHaveYouAddedAll answer" - {
      "must throw exception" in {
        intercept[NotImplementedError] {
          SUT.sanitise(emptyUserAnswers)
        }
      }
    }
  }
}
