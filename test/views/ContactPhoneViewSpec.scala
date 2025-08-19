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
import forms.ContactPhoneFormProvider
import models.ContactType.*
import models.{ContactType, NormalMode}
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.ContactPhoneView

class ContactPhoneViewSpec extends SpecBase with GuiceOneAppPerSuite {

  val SUT: ContactPhoneView = app.injector.instanceOf[ContactPhoneView]

  given request: Request[?] = FakeRequest()

  given Messages = app.injector.instanceOf[MessagesApi].preferred(request)

  val formProvider: ContactPhoneFormProvider = app.injector.instanceOf[ContactPhoneFormProvider]
  val testValue                              = "test input value"

  "ContactPhoneView" - {
    ContactType.values.foreach { contactType =>
      s"must generate a view for $contactType contact" - {
        val doc = Jsoup.parse(SUT(formProvider().bind(Map("value" -> testValue)), contactType, NormalMode).toString)

        "with the correct heading" in {
          val mainContent = doc.getElementById("main-content")

          val h1 = mainContent.getElementsByTag("h1")
          h1.size() mustBe 1

          h1.get(0).text() mustBe "Phone number"
        }

        "with the correct caption" in {
          val mainContent = doc.getElementById("main-content")

          val caption = mainContent.select("span.govuk-caption-m")
          caption.size() mustBe 1

          caption.get(0).text() mustBe (contactType match {
            case First  => "First contact details"
            case Second => "Second contact details"
            case Third  => "Third contact details"
          })
        }

        "with the correct hint" in {
          val mainContent = doc.getElementById("main-content")

          val hint = mainContent.select("div.govuk-hint")
          hint.size() mustBe 1

          hint.get(0).text() mustBe "We’ll only use this to contact you about the company’s tax accounting arrangements"
        }

        "set the input value with what is in the form" in {
          val mainContent = doc.getElementById("main-content")

          val input = mainContent.select("input")
          input.size() mustBe 1

          input.get(0).`val`() mustBe testValue
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
}
