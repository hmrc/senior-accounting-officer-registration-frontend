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
import views.html.IsCompanyEligibleView
import scala.jdk.CollectionConverters.*

class IsCompanyEligibleViewSpec extends SpecBase with GuiceOneAppPerSuite {

  val SUT: IsCompanyEligibleView = app.injector.instanceOf[IsCompanyEligibleView]
  given request: Request[_]      = FakeRequest()
  given Messages                 = app.injector.instanceOf[MessagesApi].preferred(request)

  "IsCompanyEligibleView" - {
    "must generate a view" - {
      val doc = Jsoup.parse(SUT().toString)

      "with the correct heading" in {
        val mainContent = doc.getElementById("main-content")

        val h1 = mainContent.getElementsByTag("h1")
        h1.size() mustBe 1

        h1.get(0).text() mustBe "Check if your company is eligible"
      }

      "with the correct paragraphs" in {
        val mainContent = doc.getElementById("main-content")

        val paragraphs = mainContent.getElementsByTag("p")
        paragraphs.size() mustBe 4
        List.from(paragraphs.iterator().asScala).foreach(p => p.attr("class") mustBe "govuk-body")

        paragraphs
          .get(0)
          .text() mustBe "To use this service, your company must be incorporated in the UK under the Companies Act 2006."
        paragraphs
          .get(1)
          .text() mustBe "In the previous financial year, it must either alone, or when aggregated with other UK companies in the same group, have had:"
        paragraphs
          .get(2)
          .text() mustBe "You’ll now answer a few questions to check if your company meets the eligibility rules."
        paragraphs.get(3).text() mustBe "Is this page not working properly? (opens in new tab)"
      }

      "with the correct bullet points" in {
        val mainContent = doc.getElementById("main-content")

        val ul = mainContent.getElementsByTag("ul")
        ul.size() mustBe 1
        ul.attr("class") mustBe "govuk-list govuk-list--bullet"

        val li = ul.get(0).getElementsByTag("li")
        li.size() mustBe 2

        li.get(0).text() mustBe "a turnover of more than £200 million, and, or"
        li.get(1).text() mustBe "a balance sheet total of more than £2 billion"
      }

      "must show help link" in {
        val mainContent = doc.getElementById("main-content")

        val helpLink = mainContent.getElementsByClass("govuk-link hmrc-report-technical-issue ")
        helpLink.size() mustBe 1
      }
    }
  }

}
