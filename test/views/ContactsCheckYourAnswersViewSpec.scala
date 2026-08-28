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

package views

import base.ViewSpecBase
import models.{ContactHaveYouAddedAll, ContactInfo, ContactsCheckYourAnswers}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.scalatest.Assertion
import views.ContactsCheckYourAnswersViewSpec.*
import views.html.ContactsCheckYourAnswersView

import scala.jdk.CollectionConverters.*

class ContactsCheckYourAnswersViewSpec extends ViewSpecBase[ContactsCheckYourAnswersView] {

  "ContactsCheckYourAnswersView" - {
    "must generate a view for one contact" in {
      val doc: Document = Jsoup.parse(SUT(oneContactAnswers).toString)

      doc.title() mustBe s"$pageTitle - Senior Accounting Officer notification and certificate - GOV.UK"
      doc.getMainContent.getElementsByTag("h1").text() mustBe pageHeading
      doc.getElementsByClass("govuk-back-link").size() mustBe 1
      doc.getMainContent.select("a.govuk-link.hmrc-report-technical-issue").text() mustBe
        "Is this page not working properly? (opens in new tab)"
      doc.select(".govuk-caption-l").text() mustBe pageTitle
      validateSectionHeadings(doc, List("First contact details"))
      validateSummaries(
        doc,
        expectedSections = List(
          List(
            ("Full name", "name1", "/senior-accounting-officer/registration/contact-details/first/change-name"),
            ("Email address", "email1", "/senior-accounting-officer/registration/contact-details/first/change-email"),
            (
              "Do you want to add another contact?",
              "Yes",
              "/senior-accounting-officer/registration/contact-details/first/change-add-another"
            )
          )
        )
      )

      doc.select("form").attr("action") mustBe controllers.routes.ContactCheckYourAnswersController
        .saveAndContinueReshuffled()
        .url
      doc.getElementById("submit").text() mustBe submitButtonText
    }

    "must generate a view for two contacts" in {
      val doc: Document = Jsoup.parse(SUT(twoContactAnswers).toString)

      validateSectionHeadings(doc, List("First contact details", "Second contact details"))
      validateSummaries(
        doc,
        expectedSections = List(
          List(
            ("Full name", "name1", "/senior-accounting-officer/registration/contact-details/first/change-name"),
            ("Email address", "email1", "/senior-accounting-officer/registration/contact-details/first/change-email"),
            (
              "Do you want to add another contact?",
              "No, add another contact",
              "/senior-accounting-officer/registration/contact-details/first/change-add-another"
            )
          ),
          List(
            ("Full name", "name2", "/senior-accounting-officer/registration/contact-details/second/change-name"),
            ("Email address", "email2", "/senior-accounting-officer/registration/contact-details/second/change-email")
          )
        )
      )
    }
  }

  private def validateSectionHeadings(doc: Document, expectedHeadings: List[String]): Assertion = {
    doc.getMainContent.select("h2.govuk-heading-m").eachText().asScala.toList mustBe expectedHeadings
  }

  private def validateSummaries(
      doc: Document,
      expectedSections: List[List[(String, String, String)]]
  ): Assertion = {
    val summaries = doc.getMainContent.select("dl.govuk-summary-list")
    summaries.size() mustBe expectedSections.size

    summaries.asScala.zip(expectedSections).foreach { case (summary, expectedRows) =>
      validateSummary(summary, expectedRows)
    }

    succeed
  }

  private def validateSummary(summary: Element, expectedRows: List[(String, String, String)]): Assertion = {
    val rows = summary.select("div.govuk-summary-list__row")
    rows.size() mustBe expectedRows.size

    rows.asScala.zip(expectedRows).foreach { case (row, (expectedKey, expectedValue, expectedHref)) =>
      row.select("dt.govuk-summary-list__key").text() mustBe expectedKey
      row.select("dd.govuk-summary-list__value").text() mustBe expectedValue
      row.select("dd.govuk-summary-list__actions a").attr("href") mustBe expectedHref
    }

    succeed
  }
}

object ContactsCheckYourAnswersViewSpec {
  val pageTitle: String        = "Contact details"
  val pageHeading: String      = "Check your answers"
  val submitButtonText: String = "Continue"

  val oneContactAnswers: ContactsCheckYourAnswers = ContactsCheckYourAnswers(
    firstContact = ContactInfo("name1", "email1"),
    secondContact = None,
    contactHaveYouAddedAll = ContactHaveYouAddedAll.Yes
  )

  val twoContactAnswers: ContactsCheckYourAnswers = ContactsCheckYourAnswers(
    firstContact = ContactInfo("name1", "email1"),
    secondContact = Some(ContactInfo("name2", "email2")),
    contactHaveYouAddedAll = ContactHaveYouAddedAll.No
  )
}
