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

package base

import base.ViewSpecBase.*
import org.jsoup.nodes.{Document, Element}
import org.scalactic.source.Position
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.{BaseScalaTemplate, Format, HtmlFormat}

import scala.jdk.CollectionConverters.*
import scala.reflect.ClassTag

class ViewSpecBase[T <: BaseScalaTemplate[HtmlFormat.Appendable, Format[HtmlFormat.Appendable]]: ClassTag]
    extends SpecBase
    with GuiceOneAppPerSuite {
  def SUT: T = app.injector.instanceOf[T]

  given request: Request[?] = FakeRequest()
  given Messages            = app.injector.instanceOf[MessagesApi].preferred(request)

  extension (doc: Document) {
    def getMainContent: Element = doc.getElementById("main-content")
  }

  def createTestMustHaveCorrectPageTitle(document: Document, expectedTitleContent: String)(using
      pos: Position
  ): Unit =
    val expectedTitle = s"$expectedTitleContent - $expectedServiceName - site.govuk"
    s"must generate a view with the correct title: $expectedTitle" in {
      document.title mustBe expectedTitle
    }

  def createTestMustHaveCorrectPageHeading(document: Document, headingContent: String)(using pos: Position): Unit =
    val actualH1 = document.getMainContent.getElementsByTag("h1")
    s"must generate a view with the correct page heading" in {
      withClue("the page must contain only a single <h1>\n") {
        actualH1.size() mustBe 1
      }
      actualH1.get(0).text() mustBe headingContent
    }

  def createTestMustShowIsThisPageNotWorkingProperlyLink(document: Document)(using
      pos: Position
  ): Unit =
    s"must generate a view with 'Is this page not working properly? (opens in new tab)' " in {
      val helpLink = document.getMainContent.select("a.govuk-link.hmrc-report-technical-issue")
      withClue(
        "help link not found, both contact-frontend.host and contact-frontend.serviceId must be set in the configs\n"
      ) {
        helpLink.text() mustBe "Is this page not working properly? (opens in new tab)"
        helpLink.size() mustBe 1
      }

      java.net.URI(helpLink.get(0).attributes.get("href")).getQuery must include(s"service=$expectedServiceId")
    }

  def createTestMustHaveSubmitButton(document: Document, btnContent: String)(using
      pos: Position
  ): Unit =
    s"must have a button with text '$btnContent' " in {
      val button = document.getMainContent.select("button[type=submit], input[type=submit]")
      withClue(
        s"Submit Button with text $btnContent not found\n"
      ) {
        button.text() mustBe btnContent
        button.size() mustBe 1
      }
    }

  def createTestMustShowBackLink(document: Document)(using pos: Position): Unit =
    s"must have a backlink " in {
      val backLink = document.getElementsByClass("govuk-back-link")
      withClue(
        s"backlink not found\n"
      ) {
        backLink.size() mustBe 1
      }
    }

  def createTestMustShowHeading(
      document: Document,
      content: List[String],
      selector: String,
      description: String
  )(using pos: Position): Unit = {
    mustShowElementsWithContent(document, selector, content, description)
  }

  private def mustShowElementsWithContent(
      document: Document,
      selector: String,
      expectedContent: List[String],
      description: String
  )(using pos: Position): Unit = {

    val expectedCount = expectedContent.size
    val elements      = document.getMainContent.select(selector).asScala
    s"must have $expectedCount of $description" in {
      withClue(s"Expected $expectedCount $description but found ${elements.size}\n") {
        elements.size mustBe expectedCount
      }
    }
    for (content, index) <- expectedContent.zipWithIndex do {
      s"must have a $description with content '$content' (check ${index + 1})" in {
        withClue(s"$description with content '$content' not found\n") {
          elements.zip(expectedContent).foreach { case (element, expectedText) =>
            element.text() mustEqual expectedText
          }
        }
      }
    }
  }

  def createTestMustShowInputsWithValues(
      document: Document,
      selector: String,
      expectedContent: List[String],
      description: String
  )(using pos: Position): Unit = {

    val expectedCount = expectedContent.size
    val elements      = document.getMainContent.select(selector).asScala
    s"must have $expectedCount of $description" in {
      withClue(s"Expected $expectedCount $description but found ${elements.size}\n") {
        elements.size mustBe expectedCount
      }
    }
    for (content, index) <- expectedContent.zipWithIndex do {
      s"must have a $description with content '$content' (check ${index + 1})" in {
        withClue(s"$description with content '$content' not found\n") {
          elements.zip(expectedContent).foreach { case (element, expectedText) =>
            element.attr("value") mustEqual expectedText
          }
        }
      }
    }
  }

  def createTestMustShowParagraphWithSubstring(
      document: Document,
      content: String
  )(using
      pos: Position
  ): Unit = {
    s"must have ${content.size} paragraphs which contain content: $content " in {
      val p = document.getMainContent.select(s"p:contains($content)")
      p.size() mustBe 1
    }
  }

  def createTestMustShowParagraphsWithContent(
      document: Document,
      content: List[String],
      selector: String,
      description: String
  )(using
      pos: Position
  ): Unit = {
    mustShowElementsWithContent(document, selector, content, description)
  }

  def createTestMustShowBulletPointsWithContent(
      document: Document,
      content: List[String],
      selector: String,
      description: String
  )(using
      pos: Position
  ): Unit = {
    mustShowElementsWithContent(document, selector, content, description)
  }

  def createTestMustShowLinksAndContent(
      document: Document,
      content: List[String],
      selector: String,
      description: String
  )(using
      pos: Position
  ): Unit = {
    mustShowElementsWithContent(document, selector, content, description)
  }

  def createTestMustShowHintsWithContent(
      document: Document,
      content: List[String],
      selector: String,
      description: String
  )(using pos: Position): Unit = {
    mustShowElementsWithContent(document, selector, content, description)
  }

  def createTestMustShowCaptionsWithContent(
      document: Document,
      content: List[String],
      selector: String,
      description: String
  )(using pos: Position): Unit = {
    mustShowElementsWithContent(document, selector, content, description)
  }

  def createTestMustNotShowElement(document: Document, classes: String)(using pos: Position): Unit = {
    s"must not show the element of class " in {
      val elements = document.getMainContent.getElementsByClass(classes)
      elements.size() mustBe 0
    }
  }
}

object ViewSpecBase {
  val expectedServiceName = "Senior Accounting Officer notification and certificate"
  val expectedServiceId   = "senior-accounting-officer-registration-frontend"
}
