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

package views

import base.ViewSpecBase
import models.ContactInfo
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import org.scalatest.Assertion
import views.ContactCheckYourAnswersViewSpec.*
import views.html.ContactCheckYourAnswersView

class ContactCheckYourAnswersViewSpec extends ViewSpecBase[ContactCheckYourAnswersView] {

  "ContactCheckYourAnswersView" - {

    "When exactly one contact, must generate a view" - {
      val contacts      = List(firstContact)
      val doc: Document = Jsoup.parse(SUT(contacts).toString)

      doc.createTestsWithStandardPageElements(
        pageTitle = pageHeading,
        pageHeading = pageHeading,
        showBackLink = true,
        showIsThisPageNotWorkingProperlyLink = true
      )

      val dl = doc.getMainContent.getElementsByTag("dl")

      "must show correct heading for first contact table" in {
        val previousElement     = dl.get(0).previousElementSibling()
        val previousElementText = previousElement.text()
        val previousElementTag  = previousElement.tag().toString

        withClue(s"expected heading tag h2 but found '$previousElementTag'\n") {
          previousElementTag mustBe "h2"
        }

        withClue(s"expected heading '$firstContactHeading' but found '$previousElementText'\n") {
          previousElementText mustBe firstContactHeading
        }
      }

      "must test value for one contact table" in {
        validateContactDetailsTable(dl, 0, "first", contacts.head)
      }

      "must show 1 contact table" in {
        dl.size() mustBe 1
      }

      doc.createTestsWithSubmissionButton(
        action = controllers.routes.ContactCheckYourAnswersController.saveAndContinue(),
        buttonText = submitButtonText
      )

    }

    "When exactly two contacts, must generate a view" - {
      val contacts      = List(firstContact, secondContact)
      val doc: Document = Jsoup.parse(SUT(contacts).toString)

      doc.createTestsWithStandardPageElements(
        pageTitle = pageHeading,
        pageHeading = pageHeading,
        showBackLink = true,
        showIsThisPageNotWorkingProperlyLink = true
      )

      val dl = doc.getMainContent.getElementsByTag("dl")

      "must show correct heading for first contact table" in {
        val previousElement     = dl.get(0).previousElementSibling()
        val previousElementText = previousElement.text()
        val previousElementTag  = previousElement.tag().toString

        withClue(s"expected heading tag h2 but found '$previousElementTag'\n") {
          previousElementTag mustBe "h2"
        }

        withClue(s"expected heading '$firstContactHeading' but found '$previousElementText'\n") {
          previousElementText mustBe firstContactHeading
        }
      }

      "must test values for first contact table" in {
        validateContactDetailsTable(dl, 0, "first", contacts.head)
      }

      "must show correct heading for second contact table" in {
        val previousElement     = dl.get(1).previousElementSibling()
        val previousElementText = previousElement.text()
        val previousElementTag  = previousElement.tag().toString

        withClue(s"expected heading tag h2 but found '$previousElementTag'\n") {
          previousElementTag mustBe "h2"
        }

        withClue(s"expected heading '$secondContactHeading' but found '$previousElementText'\n") {
          previousElementText mustBe secondContactHeading
        }
      }

      "must test values for second contact table" in {
        validateContactDetailsTable(dl, 1, "second", contacts.last)
      }

      "must show 2 contact tables" in {
        dl.size() mustBe 2
      }

      doc.createTestsWithSubmissionButton(
        action = controllers.routes.ContactCheckYourAnswersController.saveAndContinue(),
        buttonText = submitButtonText
      )

    }

    "When exactly three contacts, must generate a view" - {
      val contacts      = List(firstContact, secondContact, thirdContact)
      val doc: Document = Jsoup.parse(SUT(contacts).toString)

      doc.createTestsWithStandardPageElements(
        pageTitle = pageHeading,
        pageHeading = pageHeading,
        showBackLink = true,
        showIsThisPageNotWorkingProperlyLink = true
      )

      val dl = doc.getMainContent.getElementsByTag("dl")

      "must show correct heading for first contact table" in {
        val previousElement     = dl.get(0).previousElementSibling()
        val previousElementText = previousElement.text()
        val previousElementTag  = previousElement.tag().toString

        withClue(s"expected heading tag h2 but found '$previousElementTag'\n") {
          previousElementTag mustBe "h2"
        }

        withClue(s"expected heading '$firstContactHeading' but found '$previousElementText'\n") {
          previousElementText mustBe firstContactHeading
        }
      }

      "must test values for first contact table" in {
        validateContactDetailsTable(dl, 0, "first", contacts.head)
      }

      "must show correct heading for second contact table " in {
        val previousElement     = dl.get(1).previousElementSibling()
        val previousElementText = previousElement.text()
        val previousElementTag  = previousElement.tag().toString

        withClue(s"expected heading tag h2 but found '$previousElementTag'\n") {
          previousElementTag mustBe "h2"
        }

        withClue(s"expected heading '$secondContactHeading' but found '$previousElementText'\n") {
          previousElementText mustBe secondContactHeading
        }
      }

      "must test values for second contact table" in {
        validateContactDetailsTable(dl, 1, "second", contacts(1))
      }

      "must show correct heading for third contact table" in {
        val previousElement     = dl.get(2).previousElementSibling()
        val previousElementText = previousElement.text()
        val previousElementTag  = previousElement.tag().toString

        withClue(s"expected heading tag h2 but found '$previousElementTag'\n") {
          previousElementTag mustBe "h2"
        }

        withClue(s"expected heading '$thirdContactHeading' but found '$previousElementText'\n") {
          previousElementText mustBe thirdContactHeading
        }
      }

      "must test values for third contact table" in {
        validateContactDetailsTable(dl, 2, "third", contacts.last)
      }

      "must show 3 contact tables" in {
        dl.size() mustBe 3
      }

      doc.createTestsWithSubmissionButton(
        action = controllers.routes.ContactCheckYourAnswersController.saveAndContinue(),
        buttonText = submitButtonText
      )

    }
  }

  def validateContactDetailsTable(
      dl: Elements,
      tableIndex: Int,
      contactNumber: String,
      contactInfo: ContactInfo
  ): Assertion = {
    val rows = dl.get(tableIndex).select("div.govuk-summary-list__row")
    rows.size() mustBe 4
    validateRow(
      row = rows.get(0),
      keyText = "Full name",
      valueText = contactInfo.name,
      actionText = "Change",
      actionHiddenText = "change the full name",
      actionHref = s"/senior-accounting-officer/registration/contact-details/$contactNumber/change-name"
    )

    validateRow(
      row = rows.get(1),
      keyText = "Role",
      valueText = contactInfo.role,
      actionText = "Change",
      actionHiddenText = "change the role",
      actionHref = s"/senior-accounting-officer/registration/contact-details/$contactNumber/change-role"
    )

    validateRow(
      row = rows.get(2),
      keyText = "Email address",
      valueText = contactInfo.email,
      actionText = "Change",
      actionHiddenText = "change the email address",
      actionHref = s"/senior-accounting-officer/registration/contact-details/$contactNumber/change-email"
    )

    validateRow(
      row = rows.get(3),
      keyText = "Phone number",
      valueText = contactInfo.phone,
      actionText = "Change",
      actionHiddenText = "change the phone number",
      actionHref = s"/senior-accounting-officer/registration/contact-details/$contactNumber/change-phone-number"
    )

  }

  def validateRow(
      row: Element,
      keyText: String,
      valueText: String,
      actionText: String,
      actionHiddenText: String,
      actionHref: String
  ): Assertion = {
    val key = row.select("dt.govuk-summary-list__key")
    key.size() mustBe 1
    withClue("row keyText mismatch:\n") {
      key.get(0).text() mustBe keyText
    }

    val value = row.select("dd.govuk-summary-list__value")
    value.size() mustBe 1
    withClue("row valueText mismatch:\n") {
      value.get(0).text() mustBe valueText
    }

    val action = row.select("dd.govuk-summary-list__actions")
    action.size() mustBe 1

    val linkText = action.get(0).select("a")
    linkText.size() mustBe 1
    withClue("row actionHref mismatch:\n") {
      linkText.get(0).attr("href") mustBe actionHref
    }
    withClue("row actionHiddenText mismatch:\n") {
      linkText.get(0).select("span.govuk-visually-hidden").text() mustBe actionHiddenText
    }
    linkText.get(0).select("span.govuk-visually-hidden").remove()
    withClue("row actionText mismatch:\n") {
      linkText.get(0).text() mustBe actionText
    }
  }
}

object ContactCheckYourAnswersViewSpec {
  val pageHeading: String = "Check your answers"

  val firstContactHeading: String  = "First contact details"
  val secondContactHeading: String = "Second contact details"
  val thirdContactHeading: String  = "Third contact details"

  val firstContact: ContactInfo  = ContactInfo("name1", "role1", "email1", "phone1")
  val secondContact: ContactInfo = ContactInfo("name2", "role2", "email2", "phone2")
  val thirdContact: ContactInfo  = ContactInfo("name3", "role3", "email3", "phone3")

  val submitButtonText: String = "Save and Continue"
}
