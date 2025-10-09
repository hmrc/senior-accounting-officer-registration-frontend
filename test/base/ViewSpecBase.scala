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
import play.api.mvc.{Call, Request}
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

    def getConfirmationPanel: Element =
      util
        .Try(doc.select(".govuk-panel.govuk-panel--confirmation").get(0))
        .getOrElse(throw RuntimeException("No Confirmation Panel found"))

    def mustHaveCorrectPageTitle(title: String)(using pos: Position): Unit =
      "must generate a view with the correct title" in {
        doc.title mustBe s"$title - $expectedServiceName - GOV.UK"
      }

    def createTestMustHaveCorrectPageHeading(expectedHeading: String)(using
        pos: Position
    ): Unit =
      "must generate a view with the correct page heading" in {
        val actualH1 = doc.getMainContent.getElementsByTag("h1")
        withClue(s"the page must contain only a single <h1> with content '$expectedHeading'\n") {
          actualH1.get(0).text() mustBe expectedHeading
          actualH1.size() mustBe 1
        }
      }

    def createTestForBackLink(show: Boolean)(using pos: Position): Unit =
      if show then
        "must show a backlink " in {
          val backLink = doc.getElementsByClass("govuk-back-link")
          withClue(
            "backlink is not found\n"
          ) {
            backLink.size() mustBe 1
          }
        }
      else
        "must not show a backlink" in {
          val elements = doc.getElementsByClass("govuk-back-link")
          withClue(
            "a backlink was found\n"
          ) {
            elements.size() mustBe 0
          }
        }

    def createTestMustShowIsThisPageNotWorkingProperlyLink(using
        pos: Position
    ): Unit =
      "must generate a view with 'Is this page not working properly? (opens in new tab)' " in {
        val helpLink = doc.getMainContent.select("a.govuk-link.hmrc-report-technical-issue")
        withClue(
          "help link not found, both contact-frontend.host and contact-frontend.serviceId must be set in the configs\n"
        ) {
          helpLink.text() mustBe "Is this page not working properly? (opens in new tab)"
          helpLink.size() mustBe 1
        }

        java.net.URI(helpLink.get(0).attributes.get("href")).getQuery must include(s"service=$expectedServiceId")
      }

  }

  extension (target: => Document | Element) {
    private def resolve: Element = target match {
      case doc: Document => doc.getMainContent
      case _             => target
    }

    def getParagraphs(includeHelpLink: Boolean = false): Iterable[Element] =
      target.resolve.select(if includeHelpLink then "p" else excludeHelpLinkParagraphsSelector).asScala

    def getPanelTitle: Element =
      util
        .Try(target.resolve.select(".govuk-panel__title").get(0))
        .getOrElse(throw RuntimeException("No panel title found"))

    def getPanelBody: Element =
      util
        .Try(target.resolve.select(".govuk-panel__body").get(0))
        .getOrElse(throw RuntimeException("No panel body found"))

    def createTestMustShowASingleInput(
        expectedLabel: String,
        expectedValue: String,
        expectedHint: Option[String] = None
    )(using pos: Position): Unit = {

      def elements = target.resolve.select("input").asScala

      "must have a single of input" in {
        withClue(s"Expected a single input but found ${elements.size}\n") {
          elements.size mustBe 1
        }
      }

      s"must have an input with the value of '$expectedValue'" in {
        val element = elements.head
        withClue(s"input with value '$expectedValue' not found\n") {
          element.attr("value") mustEqual expectedValue
        }
      }

      s"must have a label for the input of '$expectedLabel'" in {
        val element = elements.head
        val inputId = element.attr("id")

        withClue(s"a label with '$expectedLabel' for the input is not found\n") {
          target.resolve.select(s"""label[for="$inputId"]""").text mustEqual expectedLabel
        }
      }

      expectedHint
        .map { expectedHintText =>
          s"must have a hint with values '$expectedHint'" in {
            val element = elements.head

            val hintId = element.attr("aria-describedby")

            withClue("a hint for the input is not found\n") {
              hintId must not be ""
            }

            val hints = target.resolve.select(s".govuk-hint#$hintId")
            withClue("multiple hint with the same id was found on the page\n") {
              hints.size() mustBe 1
            }

            val hint = target.resolve.select(s".govuk-hint#$hintId").get(0)

            withClue(s"hint with value '$expectedHintText' not found\n") {
              hint.text mustEqual expectedHintText
            }
          }
        }
        .getOrElse {
          "must not have an associated hint" in {
            val element = elements.head

            withClue("a hint for the input was found\n") {
              element.attr("aria-describedby") mustBe ""
            }
          }
        }
    }

    def createTestMustShowHint(expectedHint: String)(using pos: Position): Unit = {
      s"must have a hint with values '$expectedHint'" in {
        val hintElement = target.resolve.getElementsByClass("govuk-hint").asScala.headOption
        hintElement match {
          case Some(hint) => hint.text mustEqual expectedHint
          case None => fail(s"no hint element found\n")
        }
      }
    }

    def createTestMustShowTwoRadioButtons(
        expectedLabel: String,
        expectedValue: String
    )(using pos: Position): Unit = ???

    def createTestMustHaveASubmissionButtonWhichSubmitsTo(
        expectedAction: Call,
        expectedSubmitButtonText: String
    )(using
        pos: Position
    ): Unit = {
      s"must have a form which submits to '${expectedAction.method} ${expectedAction.url}'" in {
        val form = target.resolve.select("form")
        form.attr("method") mustBe expectedAction.method
        form.attr("action") mustBe expectedAction.url
        form.size() mustBe 1
      }

      s"must have a submit button with text '$expectedSubmitButtonText'" in {
        val button = target.resolve.select("button[type=submit], input[type=submit]")
        withClue(
          s"Submit Button with text $expectedSubmitButtonText not found\n"
        ) {
          button.text() mustBe expectedSubmitButtonText
          button.size() mustBe 1
        }
      }
    }

    def createTestMustHaveSubmitButton(expectedText: String)(using
        pos: Position
    ): Unit =
      s"must have a button with text '$expectedText'" in {
        val button = target.resolve.select("button[type=submit], input[type=submit]")
        withClue(
          s"Submit Button with text $expectedText not found\n"
        ) {
          button.text() mustBe expectedText
          button.size() mustBe 1
        }
      }

    def createTestMustNotShowElement(classes: String)(using pos: Position): Unit =
      s"must not show the element of class $classes" in {
        val elements = target.resolve.getElementsByClass(classes)
        elements.size() mustBe 0
      }

    def createTestMustShowText(expectedText: String)(using pos: Position): Unit =
      s"must have text '$expectedText'" in {
        target.resolve.text() mustBe expectedText
      }

    private def mustShowElementsWithContent(
        selector: String,
        expectedTexts: List[String],
        description: String
    )(using pos: Position): Unit = {
      val expectedCount = expectedTexts.size
      val elements      = target.resolve.select(selector).asScala
      s"must have $expectedCount of $description" in {
        withClue(s"Expected $expectedCount $description but found ${elements.size}\n") {
          elements.size mustBe expectedCount
        }
      }
      for (content, index) <- expectedTexts.zipWithIndex do {
        s"must have a $description with content '$content' (check ${index + 1})" in {
          withClue(s"$description with content '$content' not found\n") {
            elements.zip(expectedTexts).foreach { case (element, expectedText) =>
              element.text() mustEqual expectedText
            }
          }
        }
      }
    }

    def createTestMustShowHeadingH2s(
        expectedHeadings: List[String]
    )(using pos: Position): Unit =
      mustShowElementsWithContent(selector = "h2", expectedTexts = expectedHeadings, description = "headings")

    def createTestMustShowParagraphsWithContent(
        expectedParagraphs: List[String],
        includeHelpLink: Boolean = false
    )(using
        pos: Position
    ): Unit = {
      mustShowElementsWithContent(
        selector = if includeHelpLink then "p" else excludeHelpLinkParagraphsSelector,
        expectedTexts = expectedParagraphs,
        description = "paragraphs"
      )

      "all paragraphs must have the expected CSS class" in {
        def paragraphs =
          target.resolve.select(if includeHelpLink then "p" else excludeHelpLinkParagraphsSelector).asScala

        paragraphs.foreach(paragraph =>
          withClue(s"$paragraph did not have the expected CSS class\n") {
            paragraph.className() must include("govuk-body")
          }
        )
      }
    }

    def createTestMustShowBulletPointsWithContent(
        expectedTexts: List[String]
    )(using
        pos: Position
    ): Unit =
      mustShowElementsWithContent(
        selector = "li",
        expectedTexts = expectedTexts,
        description = "bullets"
      )

    def createTestMustShowCaptionWithContent(
        expectedCaption: String
    )(using pos: Position): Unit =
      mustShowElementsWithContent(
        selector = "span.govuk-caption-m",
        expectedTexts = List(expectedCaption),
        description = "captions"
      )

    def createTestMustShowLink(
        expectedText: String,
        expectedUrl: String
    )(using
        pos: Position
    ): Unit =
      s"must have expected link with correct text: $expectedText and correct url $expectedUrl withing provided element" in {
        val element       = target.resolve
        val link: Element = if element.tagName() == "a" then {
          element
        } else {
          val links = element.select("a").asScala
          withClue(s"Expected to find exactly one link in the element but found ${links.size}\n") {
            links.size mustBe 1
          }
          links.head
        }
        withClue(s"link text was not as expected. Got ${link.text()}, expected '$expectedText'\n") {
          link.text mustBe expectedText
        }
        withClue(s"link href was not as expected. Got ${link.attr("href")}, expected '$expectedUrl'\n") {
          link.attr("href") mustBe expectedUrl
        }

        withClue(s"link must have expected CSS class\n") {
          link.className() must include("govuk-link")
        }
      }

  }

}

object ViewSpecBase {
  val expectedServiceName               = "Senior Accounting Officer notification and certificate"
  val expectedServiceId                 = "senior-accounting-officer-registration-frontend"
  val excludeHelpLinkParagraphsSelector = "p:not(:has(a.hmrc-report-technical-issue))"
}
