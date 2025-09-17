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

import controllers.actions.*
import base.ViewSpecBase.*
import models.UserAnswers
import org.jsoup.nodes.{Document, Element}
import org.scalactic.source.Position
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.inject.bind
import play.twirl.api.{BaseScalaTemplate, Format, HtmlFormat}

import scala.jdk.CollectionConverters.*
import scala.reflect.ClassTag

class ViewSpecBase[T <: BaseScalaTemplate[HtmlFormat.Appendable, Format[HtmlFormat.Appendable]]: ClassTag]
    extends SpecBase
    with GuiceOneAppPerSuite {
  def SUT: T = app.injector.instanceOf[T]

  given request: Request[?] = FakeRequest()
  given Messages            = app.injector.instanceOf[MessagesApi].preferred(request)

  protected def fakeApplication(userAnswers: Option[UserAnswers] = None): Application =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[ApiAuthenticatedIdentifierAction].to[FakeIdentifierAction],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers))
      )
      .build()

  extension (doc: Document) {
    def getMainContent: Element = doc.getElementById("main-content")
  }

  def testMustHaveCorrectPageTitle(document: Document, title: String)(using
      pos: Position
  ): Unit =
    val expectedTitle = s"$title - $expectedServiceName - site.govuk"
    s"must generate a view with the correct title: $expectedTitle" in {
      document.title mustBe expectedTitle
    }

  def testMustHaveCorrectPageHeading(document: Document, h1: String)(using pos: Position): Unit =
    val actualH1 = document.getMainContent.getElementsByTag("h1")
    s"must generate a view with the correct page heading" in {
      withClue("the page must contain only a single <h1>\n") {
        actualH1.size() mustBe 1
      }
      actualH1.get(0).text() mustBe h1
    }

  def testMustShowIsThisPageNotWorkingProperlyLink(document: Document)(using
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

  def testMustHaveSubmitButton(document: Document, btnContent: String)(using
      pos: Position
  ): Unit =
    s"must have a button with text '$btnContent' " in {
      val button = document.getMainContent.select(s"button[type=submit], input[type=submit]")
      withClue(
        s"Submit Button with text $btnContent not found\n"
      ) {
        button.text() mustBe btnContent
        button.size() mustBe 1
      }
    }

  def testMustShowBackLink(document: Document)(using pos: Position): Unit =
    s"must have a backlink " in {
      val backLink = document.getElementsByClass("govuk-back-link")
      withClue(
        s"backlink not found\n"
      ) {
        backLink.size() mustBe 1
      }
    }

  def testMustShowHeading_h2_or_h3(document: Document, headerTag: "h2" | "h3", content: String)(using
      pos: Position
  ): Unit = {
    s"must have a heading of type $headerTag and with content: $content " in {
      val heading = document.getMainContent.select(s"$headerTag:containsOwn($content)")
      withClue(
        s"heading with tag '$headerTag' and text '$content' not found\n"
      ) {
        heading.size() mustBe 1
      }
    }
  }

  private def mustShowElementsWithContent(
      document: Document,
      selector: String,
      selectorExclusion: String,
      expectedCount: Int,
      expectedContent: List[String],
      elementType: String,
      contentExtractor: Element => String = _.text(),
      contentsSelectorBuilder: (String, String) => String = (baseSelector, content) =>
        s"$baseSelector:containsOwn($content)"
  )(using pos: Position): Unit = {

    val combinedSelector = selector + selectorExclusion
    s"must have $expectedCount of $elementType" in {

      val elements = document.getMainContent.select(combinedSelector)
      withClue(s"Expected $expectedCount $elementType but found ${elements.size()}\n") {
        elements.size() mustBe expectedCount
      }
    }
    for (textContent, index) <- expectedContent.zipWithIndex do {
      s"must have a $elementType with content '$textContent' (check ${index + 1})" in {
        val finalSelector = contentsSelectorBuilder(combinedSelector, textContent)
        val elements      = document.getMainContent.select(finalSelector)
        withClue(s"$elementType with content '$textContent' not found\n") {
          val matchingElements = elements.iterator().asScala.filter(el => contentExtractor(el) == textContent)
          matchingElements.nonEmpty mustBe true
        }
      }
    }
  }

  def testMustShowParagraphWithSubstring(
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

  def testMustShowParagraphsWithContent(
      document: Document,
      content: List[String]
  )(using
      pos: Position
  ): Unit = {
    val exclusionSelector = ":not(:has(a.hmrc-report-technical-issue)):not(:has(a))"
    mustShowElementsWithContent(document, "p", exclusionSelector, content.size, content, "paragraphs")
  }

  def testMustShowBulletPointsWithContent(
      document: Document,
      content: List[String]
  )(using
      pos: Position
  ): Unit = {
    mustShowElementsWithContent(document, "li", "", content.size, content, "bullets")
  }

  def testMustShowLinksAndContent(
      document: Document,
      content: List[String]
  )(using
      pos: Position
  ): Unit = {
    val selectExclude = ":not(a.hmrc-report-technical-issue)"
    mustShowElementsWithContent(document, "a", selectExclude, content.size, content, "links")
  }

  def testMustShowHintsWithContent(
      document: Document,
      content: List[String]
  )(using pos: Position): Unit = {
    mustShowElementsWithContent(document, "div.govuk-hint", "", content.size, content, "hints")
  }

  def testMustShowCaptionsWithContent(
      document: Document,
      content: List[String]
  )(using pos: Position): Unit = {
    mustShowElementsWithContent(document, "span.govuk-caption-m", "", content.size, content, "captions")
  }

  def testMustShowtInputsWithDefaultValues(
      document: Document,
      elementCount: Int,
      content: List[String]
  )(using pos: Position): Unit = {
    mustShowElementsWithContent(
      document,
      "input",
      "",
      content.size,
      content,
      "inputs",
      _.`val`(),
      contentsSelectorBuilder = (baseSelector, value) => s"$baseSelector[value='$value']"
    )
  }

  def testMustNotShowElement(document: Document, classes: String)(using pos: Position): Unit = {
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
