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
import views.html.ContactDetailsGuidanceView

import scala.jdk.CollectionConverters.*

class ContactDetailsGuidanceViewSpec extends SpecBase with GuiceOneAppPerSuite {

  val SUT: ContactDetailsGuidanceView = app.injector.instanceOf[ContactDetailsGuidanceView]
  given request: Request[_]           = FakeRequest()
  given Messages                      = app.injector.instanceOf[MessagesApi].preferred(request)

  "ContactDetailsGuidanceView" - {
    "must generate a view" - {
      val doc = Jsoup.parse(SUT().toString)

      "with the correct heading" in {
        val mainContent = doc.getElementById("main-content")

        val h1 = mainContent.getElementsByTag("h1")
        h1.size() mustBe 1

        h1.get(0).text() mustBe "We need contact details for this [company/group]"
      }

      "with the correct paragraphs" in {
        val mainContent = doc.getElementById("main-content")

        val paragraphs = mainContent.getElementsByTag("p")
        paragraphs.size() mustBe 4
        List.from(paragraphs.iterator().asScala).foreach(p => p.attr("class") mustBe "govuk-body")

        paragraphs
          .get(0)
          .text() mustBe "Provide HMRC with contact details for the person or team responsible for this company or group."
        paragraphs
          .get(1)
          .text() mustBe "We’ll use these details to:"
        paragraphs
          .get(2)
          .text() mustBe "You could also include the Senior Accounting Officer’s contact details so they can stay informed."
        paragraphs.get(3).text() mustBe "Is this page not working properly? (opens in new tab)"
      }

      "with the correct bullet points" in {
        val mainContent = doc.getElementById("main-content")

        val ul = mainContent.getElementsByTag("ul")
        ul.size() mustBe 1
        ul.attr("class") mustBe "govuk-list govuk-list--bullet"

        val li = ul.get(0).getElementsByTag("li")
        li.size() mustBe 2

        li.get(0)
          .text() mustBe "contact the right person if we have questions about the company’s tax accounting arrangements"
        li.get(1).text() mustBe "send confirmation when the notification and certificate have been submitted"
      }

      "must have a continue button" in {
        val mainContent = doc.getElementById("main-content")

        mainContent.getElementById("submit").text() mustBe "Continue"
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
  }
}
