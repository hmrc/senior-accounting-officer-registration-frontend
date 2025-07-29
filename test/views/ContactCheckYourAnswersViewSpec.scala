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

import base.SpecBase
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.ContactCheckYourAnswersView

import scala.jdk.CollectionConverters.*
import org.jsoup.nodes.Element
import models.ContactInfo
import ContactCheckYourAnswersViewSpec.*

class ContactCheckYourAnswersViewSpec extends SpecBase with GuiceOneAppPerSuite {

  val SUT: ContactCheckYourAnswersView = app.injector.instanceOf[ContactCheckYourAnswersView]
  given request: Request[_]            = FakeRequest()
  given Messages                       = app.injector.instanceOf[MessagesApi].preferred(request)

  "ContactCheckYourAnswersView" - {
    Map("the view has mininimum contacts" -> minContacts, 
    "the view has maximum contacts" -> maxContacts).foreach((key, contacts) => {
    s"When $key must generate a view" - {
      
      val doc = Jsoup.parse(SUT(contacts).toString)

      "with the correct heading" in {
        val mainContent = doc.getElementById("main-content")

        val h1 = mainContent.getElementsByTag("h1")
        h1.size() mustBe 1

        h1.get(0).text() mustBe "Check your answers"
      }

      "must have a continue button" in {
        val mainContent = doc.getElementById("main-content")

        mainContent.getElementById("submit").text() mustBe "Save and Continue"
      }

      "must show a back link" in {
        val backLink = doc.getElementsByClass("govuk-back-link")
        backLink.size() mustBe 1
      }

      "must show help link" in {
        val mainContent = doc.getElementById("main-content")

        val helpLink = mainContent.getElementsByClass("govuk-link hmrc-report-technical-issue ")
        helpLink.size() mustBe 1
      }
    }    
    })
    "when the view has minimum contacts" - {
      val contacts = minContacts
      val doc = Jsoup.parse(SUT(contacts).toString)


      "must show the h2" in {
        val mainContent = doc.getElementById("main-content")

        val h2 = mainContent.getElementsByTag("h2")
        h2.size() mustBe 1

        h2.get(0).text() mustBe "First contact details"
      }

      "must have correct content in table" in {
        val mainContent = doc.getElementById("main-content")
        val dl          = mainContent.getElementsByTag("dl")
        dl.size() mustBe 1
        val rows = dl.get(0).select("div.govuk-summary-list__row")

        rows.size() mustBe 4
        validateRow(
          row = rows.get(0),
          keyText = "Full name",
          valueText = "Jackson Ross",
          actionText = "Change",
          actionHiddenText = "change the full name",
          actionHref = "/senior-accounting-officer/registration/contact-details/first/change-name"
        )
        validateRow(
          row = rows.get(1),
          keyText = "Role",
          valueText = "Finance Manager",
          actionText = "Change",
          actionHiddenText = "change the role",
          actionHref = "/senior-accounting-officer/registration/contact-details/first/change-role"
        )
        validateRow(
          row = rows.get(2),
          keyText = "Email address",
          valueText = "jacksonr@abclimited.co.uk",
          actionText = "Change",
          actionHiddenText = "change the email address",
          actionHref = "/senior-accounting-officer/registration/contact-details/first/change-email"
        )
        validateRow(
          row = rows.get(3),
          keyText = "Phone number",
          valueText = "07717384239",
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
      val contacts = maxContacts
      val doc = Jsoup.parse(SUT(contacts).toString)
      "must show the h2" in {
        val mainContent = doc.getElementById("main-content")

        val h2 = mainContent.getElementsByTag("h2")
        h2.size() mustBe 3

        h2.get(0).text() mustBe "First contact details"
        h2.get(1).text() mustBe "Second contact details"
        h2.get(2).text() mustBe "Third contact details"
      }

      "must have correct content in the tables" - {
        
        val mainContent = doc.getElementById("main-content")
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
          valueText = "Jackson Ross",
          actionText = "Change",
          actionHiddenText = "change the full name",
          actionHref = "/senior-accounting-officer/registration/contact-details/first/change-name"
        )
        validateRow(
          row = rows.get(1),
          keyText = "Role",
          valueText = "Finance Manager",
          actionText = "Change",
          actionHiddenText = "change the role",
          actionHref = "/senior-accounting-officer/registration/contact-details/first/change-role"
        )
        validateRow(
          row = rows.get(2),
          keyText = "Email address",
          valueText = "jacksonr@abclimited.co.uk",
          actionText = "Change",
          actionHiddenText = "change the email address",
          actionHref = "/senior-accounting-officer/registration/contact-details/first/change-email"
        )
        validateRow(
          row = rows.get(3),
          keyText = "Phone number",
          valueText = "07717384239",
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
          valueText = "Brad B",
          actionText = "Change",
          actionHiddenText = "change the full name",
          actionHref = "/senior-accounting-officer/registration/contact-details/second/change-name"
        )
        validateRow(
          row = rows.get(1),
          keyText = "Role",
          valueText = "Architect",
          actionText = "Change",
          actionHiddenText = "change the role",
          actionHref = "/senior-accounting-officer/registration/contact-details/second/change-role"
        )
        validateRow(
          row = rows.get(2),
          keyText = "Email address",
          valueText = "b@b.bb",
          actionText = "Change",
          actionHiddenText = "change the email address",
          actionHref = "/senior-accounting-officer/registration/contact-details/second/change-email"
        )
        validateRow(
          row = rows.get(3),
          keyText = "Phone number",
          valueText = "07712345678",
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
          valueText = "a",
          actionText = "Change",
          actionHiddenText = "change the full name",
          actionHref = "/senior-accounting-officer/registration/contact-details/third/change-name"
        )
        validateRow(
          row = rows.get(1),
          keyText = "Role",
          valueText = "roleB",
          actionText = "Change",
          actionHiddenText = "change the role",
          actionHref = "/senior-accounting-officer/registration/contact-details/third/change-role"
        )
        validateRow(
          row = rows.get(2),
          keyText = "Email address",
          valueText = "email@c",
          actionText = "Change",
          actionHiddenText = "change the email address",
          actionHref = "/senior-accounting-officer/registration/contact-details/third/change-email"
        )
        validateRow(
          row = rows.get(3),
          keyText = "Phone number",
          valueText = "07712345678",
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
  val minContacts = List(ContactInfo("name1", "role1", "email1", "phone1"))
  val maxContacts = List(
    ContactInfo("name1", "role1", "email1", "phone1"),
    ContactInfo("name2", "role2", "email2", "phone2"),
    ContactInfo("name3", "role3", "email3", "phone3")
  )

}
