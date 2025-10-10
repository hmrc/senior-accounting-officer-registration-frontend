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

    def createTestsWithStandardPageElements(
        pageTitle: String,
        pageHeading: String,
        showBackLink: Boolean,
        showIsThisPageNotWorkingProperlyLink: true
    )(using pos: Position): Unit = {
      createTestWithPageTitle(pageTitle = pageTitle)
      createTestWithPageHeading(pageHeading = pageHeading)
      createTestWithBackLink(show = showBackLink)
      createTestWithIsThisPageNotWorkingProperlyLink
    }

    def createTestWithPageTitle(pageTitle: String)(using pos: Position): Unit =
      "must generate a view with the correct title" in {
        doc.title mustBe s"$pageTitle - $expectedServiceName - GOV.UK"
      }

    def createTestWithPageHeading(pageHeading: String)(using
        pos: Position
    ): Unit =
      "must generate a view with the correct page heading" in {
        val actualH1 = doc.getMainContent.getElementsByTag("h1")
        withClue(s"the page must contain only a single <h1> with content '$pageHeading'\n") {
          actualH1.get(0).text() mustBe pageHeading
          actualH1.size() mustBe 1
        }
      }

    def createTestWithBackLink(show: Boolean)(using pos: Position): Unit =
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

    def createTestWithIsThisPageNotWorkingProperlyLink(using
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

    def createTestsWithASingleTextInput(
        label: String,
        value: String,
        hint: Option[String] = None
    )(using pos: Position): Unit = {

      def elements = target.resolve.select("input").asScala

      "must have a single of input" in {
        withClue(s"Expected a single input but found ${elements.size}\n") {
          elements.size mustBe 1
        }
      }

      s"must have an input with the value of '$value'" in {
        val element = elements.head
        withClue(s"input with value '$value' not found\n") {
          element.attr("value") mustEqual value
        }
      }

      s"must have a label for the input of '$label'" in {
        val element = elements.head
        val inputId = element.attr("id")

        withClue(s"a label with '$label' for the input is not found\n") {
          target.resolve.select(s"""label[for="$inputId"]""").text mustEqual label
        }
      }

      hint
        .map { expectedHintText =>
          s"must have a hint with values '$hint'" in {
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

    def createTestsWithSubmissionButton(
        action: Call,
        buttonText: String
    )(using
        pos: Position
    ): Unit = {
      s"must have a form which submits to '${action.method} ${action.url}'" in {
        val form = target.resolve.select("form")
        form.attr("method") mustBe action.method
        form.attr("action") mustBe action.url
        form.size() mustBe 1
      }

      s"must have a submit button with text '$buttonText'" in {
        val button = target.resolve.select("button[type=submit], input[type=submit]")
        withClue(
          s"Submit Button with text $buttonText not found\n"
        ) {
          button.text() mustBe buttonText
          button.size() mustBe 1
        }
      }
    }

    def createTestWithoutElements(byClass: String)(using pos: Position): Unit =
      s"must not show the element of class $byClass" in {
        val elements = target.resolve.getElementsByClass(byClass)
        elements.size() mustBe 0
      }

    def createTestWithText(text: String)(using pos: Position): Unit =
      s"must have text '$text'" in {
        target.resolve.text() mustBe text
      }

    private def createTestWithCountOfElement(
        selector: String,
        count: Int,
        description: String
    )(using pos: Position): Unit =
      s"must have $count of $description" in {
        val elements = target.resolve.select(selector).asScala
        withClue(s"Expected $count $description but found ${elements.size}\n") {
          elements.size mustBe count
        }
      }

    private def createTestsWithOrderOfElements(
        selector: String,
        texts: List[String],
        description: String
    )(using pos: Position): Unit = {
      val expectedCount = texts.size
      val elements      = target.resolve.select(selector).asScala

      for (content, index) <- texts.zipWithIndex do {
        s"must have a $description with content '$content' (check ${index + 1})" in {
          withClue(s"$description with content '$content' not found\n") {
            elements.zip(texts).foreach { case (element, expectedText) =>
              element.text() mustEqual expectedText
            }
          }
        }
      }
    }

    def createTestsWithParagraphs(
        paragraphs: List[String]
    )(using
        pos: Position
    ): Unit = {
      createTestWithCountOfElement(
        selector = excludeHelpLinkParagraphsSelector,
        count = paragraphs.size,
        description = "paragraphs"
      )
      createTestsWithOrderOfElements(
        selector = excludeHelpLinkParagraphsSelector,
        texts = paragraphs,
        description = "paragraphs"
      )

      "all paragraphs must have the expected CSS class" in {
        def paragraphs =
          target.resolve.select(excludeHelpLinkParagraphsSelector).asScala

        paragraphs.foreach(paragraph =>
          withClue(s"$paragraph did not have the expected CSS class\n") {
            paragraph.className() must include("govuk-body")
          }
        )
      }
    }

    def createTestsWithBulletPoints(
        bullets: List[String]
    )(using
        pos: Position
    ): Unit = {
      createTestWithCountOfElement(
        selector = "li",
        count = bullets.size,
        description = "bullets"
      )
      createTestsWithOrderOfElements(
        selector = "li",
        texts = bullets,
        description = "bullets"
      )
    }

    def createTestsWithCaption(
        caption: String
    )(using pos: Position): Unit = {
      createTestWithCountOfElement(
        selector = "span.govuk-caption-m",
        count = 1,
        description = "captions"
      )
      createTestsWithOrderOfElements(
        selector = "span.govuk-caption-m",
        texts = List(caption),
        description = "captions"
      )
    }

    def createTestWithLink(
        linkText: String,
        destinationUrl: String
    )(using
        pos: Position
    ): Unit =
      s"must have expected link with correct text: $linkText and correct url $destinationUrl withing provided element" in {
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
        withClue(s"link text was not as expected. Got ${link.text()}, expected '$linkText'\n") {
          link.text mustBe linkText
        }
        withClue(s"link href was not as expected. Got ${link.attr("href")}, expected '$destinationUrl'\n") {
          link.attr("href") mustBe destinationUrl
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
