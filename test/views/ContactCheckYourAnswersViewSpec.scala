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
import models.{ContactInfo, ContactType}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import org.scalatest.Assertion
import views.ContactCheckYourAnswersViewSpec.*
import views.html.ContactCheckYourAnswersView

class ContactCheckYourAnswersViewSpec extends ViewSpecBase[ContactCheckYourAnswersView] {

  "ContactCheckYourAnswersView" - {
    ContactType.values.foreach { contactType =>
      s"When contact type is $contactType and one contact is saved, must generate a view" - {
        val contact       = firstContact
        val doc: Document = Jsoup.parse(SUT(contact, contactType).toString)

        doc.createTestsWithStandardPageElements(
          pageTitle = s"$contactType contact details",
          pageHeading = pageHeading,
          showBackLink = true,
          showIsThisPageNotWorkingProperlyLink = true,
          hasError = false
        )

        doc.createTestsWithLargeCaption(
          caption = s"$contactType contact details"
        )

        val dl = doc.getMainContent.getElementsByTag("dl")

        "must show correct caption for contact type" in {
          val caption         = doc.select(".govuk-caption-l")
          val expectedCaption = s"$contactType contact details"

          caption.size() mustBe 1
          caption.text() mustBe expectedCaption
        }

        "must test value for contact table" in {
          validateContactDetailsTable(dl, 0, contactType.toString.toLowerCase, contact)
        }

        "must show 1 contact table" in {
          dl.size() mustBe 1
        }

        doc.createTestsWithSubmissionButton(
          action = controllers.routes.ContactCheckYourAnswersController.saveAndContinue(contactType),
          buttonText = submitButtonText
        )
      }
    }
  }

  private def validateContactDetailsTable(
      dl: Elements,
      tableIndex: Int,
      contactNumber: String,
      contactInfo: ContactInfo
  ): Assertion = {
    val rows = dl.get(tableIndex).select("div.govuk-summary-list__row")
    rows.size() mustBe 2
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
      keyText = "Email address",
      valueText = contactInfo.email,
      actionText = "Change",
      actionHiddenText = "change the email address",
      actionHref = s"/senior-accounting-officer/registration/contact-details/$contactNumber/change-email"
    )
  }

  private def validateRow(
      row: Element,
      keyText: String,
      valueText: String,
      actionText: String,
      actionHiddenText: String,
      actionHref: String
  ): Assertion = {
    row.select("dt.govuk-summary-list__key").text() mustBe keyText
    row.select("dd.govuk-summary-list__value").text() mustBe valueText

    val linkText = row.select("dd.govuk-summary-list__actions a")
    linkText.attr("href") mustBe actionHref
    linkText.select("span.govuk-visually-hidden").text() mustBe actionHiddenText
    linkText.select("span.govuk-visually-hidden").remove()
    linkText.text() mustBe actionText
  }
}

object ContactCheckYourAnswersViewSpec {
  val pageHeading: String       = "Check your answers"
  val firstContact: ContactInfo = ContactInfo("name1", "email1")
  val submitButtonText: String  = "Continue"
}
