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
import org.jsoup.nodes.Element
import views.ContactCheckYourAnswersViewSpec.*
import views.html.ContactCheckYourAnswersView

class ContactCheckYourAnswersViewSpec extends ViewSpecBase[ContactCheckYourAnswersView] {

  "ContactCheckYourAnswersView" - {

    // TODO check for 1, 2, 3 contacts instead of min and max contacts
    Map("the view has minimum contacts" -> minContacts, "the view has maximum contacts" -> maxContacts).foreach(
      (scenario, contacts) => {

        val doc = Jsoup.parse(SUT(contacts).toString)

        s"When $scenario must generate a view " - {
          testMustHaveCorrectPageHeading(doc, pageHeading)
          testMustHaveSubmitButton(doc, submitButtonContent)
          testMustShowBackLink(doc)
          testMustShowIsThisPageNotWorkingProperlyLink(doc)
        }
      }
    )

    "when the view has minimum contacts" - {

      val doc = Jsoup.parse(SUT(minContacts).toString)

      "must show the heading and " - {
        testMustShowHeading_h2_or_h3(doc, "h2", "First contact details")
      }

      "must have correct content in table" in {
        val mainContent = doc.getMainContent
        val dl          = mainContent.getElementsByTag("dl")
        dl.size() mustBe 1
        val rows = dl.get(0).select("div.govuk-summary-list__row")

        rows.size() mustBe 4
        validateRow(
          row = rows.get(0),
          keyText = "Full name",
          valueText = "name1",
          actionText = "Change",
          actionHiddenText = "change the full name",
          actionHref = "/senior-accounting-officer/registration/contact-details/first/change-name"
        )
        validateRow(
          row = rows.get(1),
          keyText = "Role",
          valueText = "role1",
          actionText = "Change",
          actionHiddenText = "change the role",
          actionHref = "/senior-accounting-officer/registration/contact-details/first/change-role"
        )
        validateRow(
          row = rows.get(2),
          keyText = "Email address",
          valueText = "email1",
          actionText = "Change",
          actionHiddenText = "change the email address",
          actionHref = "/senior-accounting-officer/registration/contact-details/first/change-email"
        )
        validateRow(
          row = rows.get(3),
          keyText = "Phone number",
          valueText = "phone1",
          actionText = "Change",
          actionHiddenText = "change the phone number",
          actionHref = "/senior-accounting-officer/registration/contact-details/first/change-phone-number"
        )
        def validateRow(
            row: Element,
            keyText: String,
            valueText: String,
            actionText: String,
            actionHiddenText: String,
            actionHref: String
        ) = {
          val key = row.select("dt.govuk-summary-list__key")
          key.size() mustBe 1
          withClue("row keyText mismatch:\n") { key.get(0).text() mustBe keyText }

          val value = row.select("dd.govuk-summary-list__value")
          value.size() mustBe 1
          withClue("row valueText mismatch:\n") { value.get(0).text() mustBe valueText }

          val action = row.select("dd.govuk-summary-list__actions")
          action.size() mustBe 1

          val linkText = action.get(0).select("a")
          linkText.size() mustBe 1
          withClue("row actionHref mismatch:\n") { linkText.get(0).attr("href") mustBe actionHref }
          withClue("row actionHiddenText mismatch:\n") {
            linkText.get(0).select("span.govuk-visually-hidden").text() mustBe actionHiddenText
          }
          linkText.get(0).select("span.govuk-visually-hidden").remove()
          withClue("row actionText mismatch:\n") { linkText.get(0).text() mustBe actionText }
        }
      }

    }
    "when the view has maximum contacts" - {

      val doc = Jsoup.parse(SUT(maxContacts).toString)

      "must show heading and " - {
        testMustShowHeading_h2_or_h3(doc, "h2", "First contact details")
        testMustShowHeading_h2_or_h3(doc, "h2", "Second contact details")
        testMustShowHeading_h2_or_h3(doc, "h2", "Third contact details")
      }

      "must have correct content in the tables" - {

        val mainContent = doc.getMainContent
        val dl          = mainContent.getElementsByTag("dl")
        "must have 3 tables" in {
          dl.size() mustBe 3
        }

        "first table must have correct content" in {
          val rows = dl.get(0).select("div.govuk-summary-list__row")
          rows.size() mustBe 4
          validateRow(
            row = rows.get(0),
            keyText = "Full name",
            valueText = "name1",
            actionText = "Change",
            actionHiddenText = "change the full name",
            actionHref = "/senior-accounting-officer/registration/contact-details/first/change-name"
          )
          validateRow(
            row = rows.get(1),
            keyText = "Role",
            valueText = "role1",
            actionText = "Change",
            actionHiddenText = "change the role",
            actionHref = "/senior-accounting-officer/registration/contact-details/first/change-role"
          )
          validateRow(
            row = rows.get(2),
            keyText = "Email address",
            valueText = "email1",
            actionText = "Change",
            actionHiddenText = "change the email address",
            actionHref = "/senior-accounting-officer/registration/contact-details/first/change-email"
          )
          validateRow(
            row = rows.get(3),
            keyText = "Phone number",
            valueText = "phone1",
            actionText = "Change",
            actionHiddenText = "change the phone number",
            actionHref = "/senior-accounting-officer/registration/contact-details/first/change-phone-number"
          )
        }

        "second table must have correct content" in {
          val rows = dl.get(1).select("div.govuk-summary-list__row")
          rows.size() mustBe 4
          validateRow(
            row = rows.get(0),
            keyText = "Full name",
            valueText = "name2",
            actionText = "Change",
            actionHiddenText = "change the full name",
            actionHref = "/senior-accounting-officer/registration/contact-details/second/change-name"
          )
          validateRow(
            row = rows.get(1),
            keyText = "Role",
            valueText = "role2",
            actionText = "Change",
            actionHiddenText = "change the role",
            actionHref = "/senior-accounting-officer/registration/contact-details/second/change-role"
          )
          validateRow(
            row = rows.get(2),
            keyText = "Email address",
            valueText = "email2",
            actionText = "Change",
            actionHiddenText = "change the email address",
            actionHref = "/senior-accounting-officer/registration/contact-details/second/change-email"
          )
          validateRow(
            row = rows.get(3),
            keyText = "Phone number",
            valueText = "phone2",
            actionText = "Change",
            actionHiddenText = "change the phone number",
            actionHref = "/senior-accounting-officer/registration/contact-details/second/change-phone-number"
          )
        }

        "third table must have correct content" in {
          val rows = dl.get(2).select("div.govuk-summary-list__row")
          rows.size() mustBe 4
          validateRow(
            row = rows.get(0),
            keyText = "Full name",
            valueText = "name3",
            actionText = "Change",
            actionHiddenText = "change the full name",
            actionHref = "/senior-accounting-officer/registration/contact-details/third/change-name"
          )
          validateRow(
            row = rows.get(1),
            keyText = "Role",
            valueText = "role3",
            actionText = "Change",
            actionHiddenText = "change the role",
            actionHref = "/senior-accounting-officer/registration/contact-details/third/change-role"
          )
          validateRow(
            row = rows.get(2),
            keyText = "Email address",
            valueText = "email3",
            actionText = "Change",
            actionHiddenText = "change the email address",
            actionHref = "/senior-accounting-officer/registration/contact-details/third/change-email"
          )
          validateRow(
            row = rows.get(3),
            keyText = "Phone number",
            valueText = "phone3",
            actionText = "Change",
            actionHiddenText = "change the phone number",
            actionHref = "/senior-accounting-officer/registration/contact-details/third/change-phone-number"
          )
        }

        def validateRow(
            row: Element,
            keyText: String,
            valueText: String,
            actionText: String,
            actionHiddenText: String,
            actionHref: String
        ) = {
          val key = row.select("dt.govuk-summary-list__key")
          key.size() mustBe 1
          withClue("row keyText mismatch:\n") { key.get(0).text() mustBe keyText }

          val value = row.select("dd.govuk-summary-list__value")
          value.size() mustBe 1
          withClue("row valueText mismatch:\n") { value.get(0).text() mustBe valueText }

          val action = row.select("dd.govuk-summary-list__actions")
          action.size() mustBe 1

          val linkText = action.get(0).select("a")
          linkText.size() mustBe 1
          withClue("row actionHref mismatch:\n") { linkText.get(0).attr("href") mustBe actionHref }
          withClue("row actionHiddenText mismatch:\n") {
            linkText.get(0).select("span.govuk-visually-hidden").text() mustBe actionHiddenText
          }
          linkText.get(0).select("span.govuk-visually-hidden").remove()
          withClue("row actionText mismatch:\n") { linkText.get(0).text() mustBe actionText }
        }
      }
    }
  }
}

object ContactCheckYourAnswersViewSpec {
  val minContacts: List[ContactInfo] = List(ContactInfo("name1", "role1", "email1", "phone1"))
  val maxContacts: List[ContactInfo] = minContacts ++ List(
    ContactInfo("name2", "role2", "email2", "phone2"),
    ContactInfo("name3", "role3", "email3", "phone3")
  )
  val pageHeading         = "Check your answers"
  val submitButtonContent = "Save and Continue"

}
