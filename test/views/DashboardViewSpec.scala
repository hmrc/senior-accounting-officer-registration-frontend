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
import models.DashboardStage
import models.DashboardStage.{CompanyDetails, ContactsInfo, Submission}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.DashboardViewSpec.*
import views.html.DashboardView

class DashboardViewSpec extends ViewSpecBase[DashboardView] {

  "DashboardView" - {

    DashboardStage.values.foreach { stage =>
      s"must generate a view for $stage stage" - {
        val doc: Document = Jsoup.parse(SUT(stage).toString)

        doc.createTestsWithStandardPageElements(
          pageTitle = pageHeading,
          pageHeading = pageHeading,
          showBackLink = false,
          showIsThisPageNotWorkingProperlyLink = true,
          hasError = false
        )

        doc.createTestsWithParagraphs(paragraphs = paragraphs)

        "must show the correct statuses" in {
          val statusTags = doc.getMainContent.getElementsByClass("govuk-task-list__status")
          statusTags.size() mustBe 2

          val companyDetailsTag = statusTags.get(0)
          val contactsInfoTag   = statusTags.get(1)

          stage match {
            case CompanyDetails =>
              companyDetailsTag.text() mustBe "Not started"
              companyDetailsTag.getElementsByTag("strong").attr("class") mustBe "govuk-tag govuk-tag--blue"
              contactsInfoTag.text() mustBe "Cannot start yet"
              contactsInfoTag.getElementsByTag("strong").size() mustBe 0
            case ContactsInfo =>
              companyDetailsTag.text() mustBe "Completed"
              contactsInfoTag.text() mustBe "Not started"
              contactsInfoTag.getElementsByTag("strong").attr("class") mustBe "govuk-tag govuk-tag--blue"
            case Submission =>
              companyDetailsTag.text() mustBe "Completed"
              contactsInfoTag.text() mustBe "Completed"
          }
        }

        stage match {
          case CompanyDetails =>
            doc.createTestWithoutElements(byClass = "govuk-button")
            doc.getMainContent
              .selectFirst("li a[aria-describedby='company-details-status']")
              .createTestWithLink(
                linkText = enterNominatedCompanyDetailsLinkText,
                destinationUrl = enterNominatedCompanyDetailsLinkUrl
              )
          case ContactsInfo =>
            doc.createTestWithoutElements(byClass = "govuk-button")
            doc.getMainContent
              .selectFirst("li a[aria-describedby='contacts-details-status']")
              .createTestWithLink(
                linkText = enterYourContactDetailsLinkText,
                destinationUrl = enterYourContactDetailsLinkUrl
              )
          case Submission =>
            doc.createTestsWithSubmissionButton(
              action = controllers.routes.IndexController.continue(),
              buttonText = submitButtonText
            )
        }

      }
    }
  }
}

object DashboardViewSpec {
  val pageHeading: String                  = "Register your nominated company"
  val enterNominatedCompanyDetailsLinkText = "Enter your nominated company details"
  val enterNominatedCompanyDetailsLinkUrl = "/senior-accounting-officer/registration/nominated-company-details-guidance"
  val enterYourContactDetailsLinkText     = "Enter your contact details"
  val enterYourContactDetailsLinkUrl      = "/senior-accounting-officer/registration/contact-details"
  val paragraphs: List[String]            = List(
    "Register the nominated UK company that will submit the SAO notification and certificate on behalf of all companies the SAO is responsible for.",
    "You only need to register once for your group. If your group has more than one SAO, you can use the same account to submit notifications and certificates for all SAOs, or register again to create a separate account."
  )

  val submitButtonText: String = "Submit your registration"

}
