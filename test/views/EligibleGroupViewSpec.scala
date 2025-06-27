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
import views.html.EligibleGroupView

import scala.jdk.CollectionConverters.*

class EligibleGroupViewSpec extends SpecBase with GuiceOneAppPerSuite {

  val SUT: EligibleGroupView = app.injector.instanceOf[EligibleGroupView]
  given request: Request[_]  = FakeRequest()
  given Messages             = app.injector.instanceOf[MessagesApi].preferred(request)

  "EligibleGroupView" - {
    "must generate a view" - {
      val doc = Jsoup.parse(SUT().toString)

      "with the correct heading" in {
        val mainContent = doc.getElementById("main-content")

        val h1 = mainContent.getElementsByTag("h1")
        h1.size() mustBe 1

        h1.get(0).text() mustBe "You need to register this Group to submit a Senior Accounting Officer notification and certificate"
      }

      "with the correct paragraphs" in {
        val mainContent = doc.getElementById("main-content")

        val paragraphs = mainContent.getElementsByTag("p")
        paragraphs.size() mustBe 2
        List.from(paragraphs.iterator().asScala).foreach(p => p.attr("class") mustBe "govuk-body")

        paragraphs
          .get(0)
          .text() mustBe "You now need to sign in with an organisation or individual Government Gateway user ID associated with your company."
        paragraphs.get(1).text() mustBe "Is this page not working properly? (opens in new tab)"
      }

      "with the correct warning message" in {
        val mainContent = doc.getElementById("main-content")

        val warning = mainContent.select("strong.govuk-warning-text__text")
        warning.size() mustBe 1
        val visuallyHidden = "Warning"
        warning.select("span.govuk-visually-hidden").text() mustBe visuallyHidden
        warning.text() mustBe s"$visuallyHidden You cannot use an agent Government Gateway user ID to register."
      }

      "must show help link" in {
        val mainContent = doc.getElementById("main-content")

        val helpLink = mainContent.getElementsByClass("govuk-link hmrc-report-technical-issue ")
        helpLink.size() mustBe 1
      }
    }
  }

}
