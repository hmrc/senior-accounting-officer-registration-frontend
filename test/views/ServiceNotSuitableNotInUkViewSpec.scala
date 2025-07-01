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
import views.html.ServiceNotSuitableNotInUkView

import scala.jdk.CollectionConverters.*

class ServiceNotSuitableNotInUkViewSpec extends SpecBase with GuiceOneAppPerSuite {

  val SUT: ServiceNotSuitableNotInUkView =
    app.injector.instanceOf[ServiceNotSuitableNotInUkView]
  given request: Request[_] = FakeRequest()
  given Messages            = app.injector.instanceOf[MessagesApi].preferred(request)

  "ServiceNotSuitableNotInUkView" - {
    "must generate a view" - {
      val doc = Jsoup.parse(SUT().toString)

      "with the correct heading" in {
        val mainContent = doc.getElementById("main-content")

        val h1 = mainContent.getElementsByTag("h1")
        h1.size() mustBe 1

        h1.get(0)
          .text() mustBe "Your company does not need to submit a Senior Accounting Officer notification and certificate in the UK"
      }

      "with the correct paragraphs" in {
        val mainContent = doc.getElementById("main-content")

        val paragraphs = mainContent.getElementsByTag("p")
        paragraphs.size() mustBe 4
        List.from(paragraphs.iterator().asScala).foreach(p => p.attr("class") mustBe "govuk-body")

        paragraphs
          .get(0)
          .text() mustBe "companies that are incorporated in the UK under the Companies Act 2006 need to submit a notification and certificate."
        paragraphs
          .get(1)
          .text() mustBe "If you are part of a group that includes companies outside the UK, you should check if any of them have a liability in the UK."

        val linkParagraph = paragraphs.get(2)
        val link1         = linkParagraph.select("a.govuk-link")
        link1.size() mustBe 1
        link1.get(0).attr("href") mustBe "#"
        link1.text() mustBe "Find out more about who is eligible for this service"

        paragraphs.get(3).text() mustBe "Is this page not working properly? (opens in new tab)"
      }

      "must show help link" in {
        val mainContent = doc.getElementById("main-content")

        val helpLink = mainContent.getElementsByClass("govuk-link hmrc-report-technical-issue ")
        helpLink.size() mustBe 1
      }
    }
  }

}
