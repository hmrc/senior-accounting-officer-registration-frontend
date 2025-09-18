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

    Map(
      "the view has one contact"    -> oneContact,
      "the view has two contacts"   -> twoContacts,
      "the view has three contacts" -> threeContacts
    )
      .foreach((scenario, contacts) =>
        val doc: Document = Jsoup.parse(SUT(contacts).toString)
        s"When $scenario must generate a view" - {
          createTestMustHaveCorrectPageHeading(doc, pageHeading)
          createTestMustHaveSubmitButton(doc, submitButtonContent)
          createTestMustShowBackLink(doc)
          createTestMustShowIsThisPageNotWorkingProperlyLink(doc)
          performTests(doc, scenario)
        }
      )

    def performTests(doc: Document, key: String): Unit = {

      key match {
        case "the view has one contact" =>
          createTestMustShowHeading(doc, headings.take(1), "h2", "headings")
          val mainContent = doc.getMainContent
          val dl          = mainContent.getElementsByTag("dl")
          dl.size() mustBe 1
          s"must test value for one contact " in {
            validateContactDetailsTable(dl, 0, "first", oneContact.head)
          }

        case "the view has two contacts" =>
          createTestMustShowHeading(doc, headings.take(2), "h2", "headings")
          val mainContent = doc.getMainContent
          val dl          = mainContent.getElementsByTag("dl")
          dl.size() mustBe 2
          s"must test values for 2 contacts " in {
            validateContactDetailsTable(dl, 0, "first", oneContact.head)
            validateContactDetailsTable(dl, 1, "second", twoContacts.last)
          }
        case "the view has three contacts" =>
          createTestMustShowHeading(
            doc,
            headings,
            "h2",
            "headings"
          )
          val mainContent = doc.getMainContent
          val dl          = mainContent.getElementsByTag("dl")
          dl.size() mustBe 3
          s"must test values for 3 contacts " in {
            validateContactDetailsTable(dl, 0, "first", oneContact.head)
            validateContactDetailsTable(dl, 1, "second", twoContacts.last)
            validateContactDetailsTable(dl, 2, "third", threeContacts.last)
          }
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
}

object ContactCheckYourAnswersViewSpec {
  val oneContact: List[ContactInfo]    = List(ContactInfo("name1", "role1", "email1", "phone1"))
  val twoContacts: List[ContactInfo]   = oneContact ++ List(ContactInfo("name2", "role2", "email2", "phone2"))
  val threeContacts: List[ContactInfo] = twoContacts ++ List(ContactInfo("name3", "role3", "email3", "phone3"))

  val headings: List[String] = List("First contact details", "Second contact details", "Third contact details")

  val pageHeading         = "Check your answers"
  val submitButtonContent = "Save and Continue"

}
