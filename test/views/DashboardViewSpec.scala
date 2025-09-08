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
import models.DashboardStage
import models.DashboardStage.{CompanyDetails, ContactsInfo, Submission}
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.DashboardView

import scala.jdk.CollectionConverters.*

class DashboardViewSpec extends SpecBase with GuiceOneAppPerSuite {

  val SUT: DashboardView    = app.injector.instanceOf[DashboardView]
  given request: Request[?] = FakeRequest()
  given Messages            = app.injector.instanceOf[MessagesApi].preferred(request)

  "DashboardView" - {

    DashboardStage.values.foreach { stage =>
      s"must generate a view for $stage stage" - {
        val doc = Jsoup.parse(SUT(stage).toString)

        "with the correct heading" in {
          val mainContent = doc.getElementById("main-content")

          val h1 = mainContent.getElementsByTag("h1")
          h1.size() mustBe 1

          h1.get(0).text() mustBe "Register your company"
        }

        "with the correct paragraphs" in {
          val mainContent = doc.getElementById("main-content")

          val paragraphs = mainContent.getElementsByTag("p")
          paragraphs.size() mustBe 3
          List.from(paragraphs.iterator().asScala).foreach(p => p.attr("class") mustBe "govuk-body")

          paragraphs
            .get(0)
            .text() mustBe "Register the company responsible for submitting the Senior Accounting Officer (SAO) notification and certificate. There’s no required company type. This should be the company where the SAO works from."
          paragraphs
            .get(1)
            .text() mustBe "If your group has more than one SAO, you’ll need to complete a separate registration for each SAO."

          paragraphs.get(2).text() mustBe "Is this page not working properly? (opens in new tab)"
        }

        "with the correct link texts" in {
          val mainContent = doc.getElementById("main-content")

          val links = mainContent.getElementsByClass("govuk-link govuk-task-list__link")

          stage match {
            case CompanyDetails =>
              links.size() mustBe 1
              links.get(0).text() mustBe "Enter your company details"
            case ContactsInfo =>
              links.size() mustBe 1
              links.get(0).text() mustBe "Enter your contact details"
            case Submission =>
              links.size() mustBe 0
          }
        }

        "with the correct label texts" in {
          val mainContent = doc.getElementById("main-content")
          val statusTags  = mainContent.getElementsByClass("govuk-task-list__status")

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
              companyDetailsTag.getElementsByTag("strong").attr("class") mustBe "govuk-tag govuk-tag--green"
              contactsInfoTag.text() mustBe "Not started"
              contactsInfoTag.getElementsByTag("strong").attr("class") mustBe "govuk-tag govuk-tag--blue"
            case Submission =>
              companyDetailsTag.text() mustBe "Completed"
              companyDetailsTag.getElementsByTag("strong").attr("class") mustBe "govuk-tag govuk-tag--green"
              contactsInfoTag.text() mustBe "Completed"
              contactsInfoTag.getElementsByTag("strong").attr("class") mustBe "govuk-tag govuk-tag--green"
          }
        }

        stage match {
          case Submission =>
            "must show a submit button" in {
              val backLink = doc.getElementById("submit")
              backLink.text() mustBe "Submit your registration"
            }
          case _ =>
            "must not show a submit button" in {
              val backLink = doc.getElementById("submit")
              Option(backLink) mustBe None
            }
        }

        "must not show a back link" in {
          val backLink = doc.getElementsByClass("govuk-back-link")
          backLink.size() mustBe 0
        }

        "must show help link" in {
          val mainContent = doc.getElementById("main-content")

          val helpLink = mainContent.getElementsByClass("govuk-link hmrc-report-technical-issue ")
          helpLink.size() mustBe 1
        }

      }
    }
  }

}
