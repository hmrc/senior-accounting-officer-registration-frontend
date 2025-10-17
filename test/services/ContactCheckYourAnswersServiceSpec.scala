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

package services

import base.SpecBase
import models.*
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages.*
import services.ContactCheckYourAnswersServiceSpec.*

class ContactCheckYourAnswersServiceSpec extends SpecBase with GuiceOneAppPerSuite {
  val SUT: ContactCheckYourAnswersService = app.injector.instanceOf[ContactCheckYourAnswersService]

  "ContactCheckYourAnswersService.GetContactInfos" - {
    "given no user answers" - {
      "then return empty list" in {
        val userAnswers = emptyUserAnswers
        val result      = SUT.getContactInfos(userAnswers)
        result mustBe List.empty
      }
    }
    "given userAnswers has a complete set of first contact info" - {
      "then return a list with one contact info" in {
        val userAnswers = emptyUserAnswers
          .updateContact(ContactType.First, "name", "email")
        val result = SUT.getContactInfos(userAnswers)
        result mustBe List(ContactInfo("name", "email"))
      }
    }
    "given userAnswers has a complete set of first & second contact info" - {
      "when the user answer for `haveYouAnsweredAllContact(First)` is no" - {
        "then return a list with first and second contact info" in {
          val userAnswers = emptyUserAnswers
            .updateContact(ContactType.First, "name", "email")
            .updateContact(ContactType.First, haveYouAddedAllContacts = false)
            .updateContact(ContactType.Second, "name2", "email2")

          val result = SUT.getContactInfos(userAnswers)
          result mustBe List(
            ContactInfo("name", "email"),
            ContactInfo("name2", "email2")
          )
        }
      }
      "when the user answer for `haveYouAnsweredAllContact(First)` is yes" - {
        "then return a list with only the first contact info" in {
          val userAnswers = emptyUserAnswers
            .updateContact(ContactType.First, "name", "email")
            .updateContact(ContactType.First, haveYouAddedAllContacts = true)
            .updateContact(ContactType.Second, "name2", "email2")

          val result = SUT.getContactInfos(userAnswers)
          result mustBe List(
            ContactInfo("name", "email")
          )
        }
      }
      "when the user answer for `haveYouAnsweredAllContact(First)` is empty" - {
        "then return a list with only the first contact info" in {
          val userAnswers = emptyUserAnswers
            .updateContact(ContactType.First, "name", "email")
            .updateContact(ContactType.Second, "name2", "email2")

          val result = SUT.getContactInfos(userAnswers)
          result mustBe List(
            ContactInfo("name", "email")
          )
        }
      }
      "when the user answer for `haveYouAnsweredAllContact(Second)` is empty" - {
        "then return a list with the first & second contact info" in {
          val userAnswers = emptyUserAnswers
            .updateContact(ContactType.First, "name", "email")
            .updateContact(ContactType.First, haveYouAddedAllContacts = false)
            .updateContact(ContactType.Second, "name2", "email2")

          val result = SUT.getContactInfos(userAnswers)
          result mustBe List(
            ContactInfo("name", "email"),
            ContactInfo("name2", "email2")
          )
        }
      }
    }
  }

  "ContactCheckYourAnswersService.GetContactInfo" - {

    ContactType.values.foreach((contactType) => {

      s"when getContactInfo (userAnswers, $contactType) is called" - {

        "when userAnswer has all contact info then return ContactInfo" in {

          val userAnswers = emptyUserAnswers
            .updateContact(contactType, "name", "email")

          val result = SUT.getContactInfo(userAnswers, contactType)

          result mustBe Some(ContactInfo("name", "email"))
        }
        "when userAnswer has no name then return None" in {
          val userAnswers = emptyUserAnswers
            .updateContact(contactType, None, Some("email"))
          val result = SUT.getContactInfo(userAnswers, contactType)
          result mustBe None
        }
        "when userAnswer has no email then return None" in {
          val userAnswers = emptyUserAnswers
            .updateContact(contactType, Some("name"), None)
          val result = SUT.getContactInfo(userAnswers, contactType)
          result mustBe None
        }
      }
    })
  }
}

object ContactCheckYourAnswersServiceSpec {
  extension (userAnswers: UserAnswers) {
    def updateContact(
        contactType: ContactType.First.type | ContactType.Second.type,
        haveYouAddedAllContacts: Boolean
    ): UserAnswers = {
      userAnswers
        .set(
          ContactHaveYouAddedAllPage(contactType),
          if haveYouAddedAllContacts then ContactHaveYouAddedAll.Yes else ContactHaveYouAddedAll.No
        )
        .get
    }

    def updateContact(
        contactType: ContactType,
        name: String,
        email: String
    ): UserAnswers = {
      updateContact(contactType, Some(name), Some(email))
    }
    def updateContact(
        contactType: ContactType,
        name: Option[String],
        email: Option[String]
    ): UserAnswers = {
      List(name, email).zipWithIndex
        .foldLeft(userAnswers)((accumulator, configs) => {
          configs match {
            case Some(value) -> 0 => accumulator.set(ContactNamePage(contactType), value).get
            case Some(value) -> 1 => accumulator.set(ContactEmailPage(contactType), value).get
            case _                => accumulator
          }
        })
    }
  }
}
