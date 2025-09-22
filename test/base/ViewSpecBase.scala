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

  def createTestMustHaveCorrectPageHeading(document: Document, expectedHeadingContent: String)(using
      pos: Position
  ): Unit =
    val actualH1 = document.getMainContent.getElementsByTag("h1")
    "must generate a view with the correct page heading" in {
      withClue("the page must contain only a single <h1>\n") {
        actualH1.size() mustBe 1
      }
      actualH1.get(0).text() mustBe expectedHeadingContent
    }

  def createTestMustShowIsThisPageNotWorkingProperlyLink(document: Document)(using
      pos: Position
  ): Unit =
    "must generate a view with 'Is this page not working properly? (opens in new tab)' " in {
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
    "must have a backlink " in {
      val backLink = document.getElementsByClass("govuk-back-link")
      withClue(
        "backlink not found\n"
      ) {
        backLink.size() mustBe 1
      }
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

  def createTestMustShowHeadingH2(
      document: Document,
      expectedContent: List[String]
  )(using pos: Position): Unit = {
    mustShowElementsWithContent(document = document, selector = "h2", expectedContent = expectedContent, "headings")
  }

  def createTestMustShowInputsWithValues(
      document: Document,
      expectedValues: List[String]
  )(using pos: Position): Unit = {

    val expectedCount = expectedValues.size
    val elements      = document.getMainContent.select("input").asScala
    s"must have $expectedCount of inputs" in {
      withClue(s"Expected $expectedCount inputs but found ${elements.size}\n") {
        elements.size mustBe expectedCount
      }
    }
    for (value, index) <- expectedValues.zipWithIndex do {
      s"must have a inputs with values '$value' (check ${index + 1})" in {
        withClue(s"input with value '$value' not found\n") {
          elements.zip(expectedValues).foreach { case (element, expectedValueText) =>
            element.attr("value") mustEqual expectedValueText
          }
        }
      }
    }
  }

  def createTestMustShowParagraphsWithContent(
      document: Document,
      expectedParagraphs: List[String],
      includeHelpLink: Boolean = false
  )(using
      pos: Position
  ): Unit =
    mustShowElementsWithContent(
      document = document,
      selector = if includeHelpLink then "p" else excludeHelpLinkParagraphsSelector,
      expectedContent = expectedParagraphs,
      description = "paragraphs"
    )

  def createTestMustShowPanelHeadingsWithContent(
      document: Document,
      expectedPanelHeadings: List[String]
  )(using
      pos: Position
  ): Unit =
    mustShowElementsWithContent(
      document = document,
      selector = "div.govuk-panel__body",
      expectedContent = expectedPanelHeadings,
      description = "panel headings"
    )

  def createTestMustShowBulletPointsWithContent(
      document: Document,
      expectedContentList: List[String]
  )(using
      pos: Position
  ): Unit =
    mustShowElementsWithContent(
      document = document,
      selector = "li",
      expectedContent = expectedContentList,
      description = "bullets"
    )

  def createTestMustShowLink(
      element: => Element,
      expectedContent: String,
      expectedUrl: String
  )(using
      pos: Position
  ): Unit =
    s"must have expected link with correct content: $expectedContent and correct url $expectedUrl withing provided element" in {
      val link: Element = if element.tagName() == "a" then {
        element
      } else {
        val links = element.select("a").asScala
        withClue(s"Expected to find exactly one link in the element but found ${links.size}\n") {
          links.size mustBe 1
        }
        links.head
      }
      withClue(s"links content was not as expected. Got ${link.text()}, expected '$expectedContent''") {
        link.text mustBe expectedContent
      }
      withClue(s"links href was not as expected. Got ${link.attr("href")}, expected '$expectedUrl''") {
        link.attr("href") mustBe expectedUrl
      }
    }

  def createTestMustShowHintsWithContent(
      document: Document,
      expectedContent: List[String]
  )(using pos: Position): Unit =
    mustShowElementsWithContent(
      document = document,
      selector = "div.govuk-hint",
      expectedContent = expectedContent,
      description = "hints"
    )

  def createTestMustShowCaptionsWithContent(
      document: Document,
      expectedContent: List[String]
  )(using pos: Position): Unit =
    mustShowElementsWithContent(
      document = document,
      selector = "span.govuk-caption-m",
      expectedContent = expectedContent,
      description = "captions"
    )

  def createTestMustNotShowElement(document: Document, classes: String)(using pos: Position): Unit = {
    "must not show the element of class " in {
      val elements = document.getMainContent.getElementsByClass(classes)
      elements.size() mustBe 0
    }
  }
}

object ViewSpecBase {
  val expectedServiceName               = "Senior Accounting Officer notification and certificate"
  val expectedServiceId                 = "senior-accounting-officer-registration-frontend"
  val excludeHelpLinkParagraphsSelector = "p:not(:has(a.hmrc-report-technical-issue))"
  val excludeHelpLinkLinkSelector       = "a[href]:not(a.hmrc-report-technical-issue)"
}
