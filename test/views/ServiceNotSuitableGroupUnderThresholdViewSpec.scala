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
import views.html.ServiceNotSuitableGroupUnderThresholdView

import scala.jdk.CollectionConverters.*

class ServiceNotSuitableGroupUnderThresholdViewSpec extends SpecBase with GuiceOneAppPerSuite {

  val SUT: ServiceNotSuitableGroupUnderThresholdView =
    app.injector.instanceOf[ServiceNotSuitableGroupUnderThresholdView]
  given request: Request[_] = FakeRequest()
  given Messages            = app.injector.instanceOf[MessagesApi].preferred(request)

  "ServiceNotSuitableGroupUnderThresholdView" - {
    "must generate a view" - {
      val doc = Jsoup.parse(SUT().toString)

      "with the correct heading" in {
        val mainContent = doc.getElementById("main-content")

        val h1 = mainContent.getElementsByTag("h1")
        h1.size() mustBe 1

        h1.get(0)
          .text() mustBe "Your group does not need to submit a Senior Accounting Officer notification and certificate"
      }

      "with the correct paragraphs" in {
        val mainContent = doc.getElementById("main-content")

        val paragraphs = mainContent.getElementsByTag("p")
        paragraphs.size() mustBe 4
        List.from(paragraphs.iterator().asScala).foreach(p => p.attr("class") mustBe "govuk-body")

        paragraphs
          .get(0)
          .text() mustBe "The SAO requirements apply to UK-incorporated companies or groups that, in the previous financial year, had either:"
        paragraphs
          .get(1)
          .text() mustBe "If your group meets either of these thresholds in a future financial year, you may need to submit a notification and certificate then."

        val linkParagraph = paragraphs.get(2)
        val link1         = linkParagraph.select("a.govuk-link")
        link1.size() mustBe 1
        link1.get(0).attr("href") mustBe "#"
        link1.text() mustBe "Find out more about who is eligible for the Senior Accounting Officer notification and certificate service"

        paragraphs.get(3).text() mustBe "Is this page not working properly? (opens in new tab)"
      }

      "with the correct bullet points" in {
        val mainContent = doc.getElementById("main-content")

        val ul = mainContent.getElementsByTag("ul")
        ul.size() mustBe 1
        ul.attr("class") mustBe "govuk-list govuk-list--bullet"

        val li = ul.get(0).getElementsByTag("li")
        li.size() mustBe 2

        li.get(0).text() mustBe "a turnover of more than £200 million, or"
        li.get(1).text() mustBe "a balance sheet total of more than £2 billion."
      }

      "must show help link" in {
        val mainContent = doc.getElementById("main-content")

        val helpLink = mainContent.getElementsByClass("govuk-link hmrc-report-technical-issue ")
        helpLink.size() mustBe 1
      }
    }
  }

}
