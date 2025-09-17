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

  def testMustHaveCorrectPageTitle(document: Document, title: String)(using
      pos: Position
  ): Unit =
    val expectedTitle = s"$title - $expectedServiceName - site.govuk"
    s"must generate a view with the correct title: $expectedTitle" in {
      document.title mustBe expectedTitle
    }

  def testMustHaveCorrectPageHeading(document: Document, h1: String)(using pos: Position): Unit =
    val actualH1 = document.getMainContent.getElementsByTag("h1")
    s"must generate a view with the correct page heading $actualH1" in {
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

  def testMustShowElements(document: Document, selectorString: String, expectedCount: Int, description: String)(using
      pos: Position
  ): Unit = {
    s"must have the correct number of $description ($expectedCount)" in {
      val elements = document.getMainContent.select(selectorString)
      withClue(s"Expected $expectedCount $description but not found ${elements.size()}\n") {
        elements.size() mustBe expectedCount
      }
    }
  }

  def mustShowElementsWithContent(
      document: Document,
      selectorString: String,
      expectedContent: List[String],
      description: String
  )(using pos: Position): Unit = {
    for content <- expectedContent do
      s"must have the $description with content '$expectedContent''" in {
        val elements = document.getMainContent.select(s"$selectorString:containsOwn($content)")
        withClue(s"Expected $description with content '$content' not found\n") {
          elements.get(0).text() mustBe content
          elements.size() mustBe 1
        }
      }
  }

  def mustShowParagraphsContainingContent(
      document: Document,
      paragraphsCount: Int,
      content: String
  )(using
      pos: Position
  ): Unit = {
    s"must have $paragraphsCount paragraphs which contain content: $content " in {
      val p = document.getMainContent.select(s"p:contains($content)")
      p.size() mustBe paragraphsCount
    }
  }

  def testMustShowParagraphsWithContent(
      document: Document,
      expectedCount: Int,
      content: List[String]
  )(using
      pos: Position
  ): Unit = {

    testMustShowElements(document, "p", expectedCount, "paragraphs")
    mustShowElementsWithContent(document, "p", content, "paragraphs")
  }

  def testMustShowCorrectParagraphsWithCorrectContent(
      document: Document,
      paragraphsCount: Int,
      content: List[String]
  )(using
      pos: Position
  ): Unit = {
    s"must have total $paragraphsCount paragraphs" in {
      val p = document.getMainContent.getElementsByTag("p")
      p.size() mustBe paragraphsCount
    }
    for paragraphContent <- content do
      s"must have a paragraph with content: '$paragraphContent'" in {
        val paragraph = document.getMainContent.select(s"p:containsOwn($paragraphContent)")
        withClue(
          s"paragraph with content '$paragraphContent' not found\n"
        ) {
          paragraph.size() mustBe 1
          paragraph.get(0).text() mustBe paragraphContent
        }
      }
  }

  def testMustShowCorrectBulletPointsWithCorrectContent(
      document: Document,
      bulletCount: Int,
      content: List[String]
  )(using
      pos: Position
  ): Unit = {
    s"must have $bulletCount bulletPoints" in {
      val p = document.getMainContent.getElementsByTag("li")
      p.size() mustBe bulletCount
    }
    for bulletContent <- content do
      s"must have bulletPoints at with content: '$bulletContent'and" in {

        val bullet = document.getMainContent.select(s"li:containsOwn($bulletContent)")
        withClue(
          s"bulletPoint with content '$bulletContent' not found\n"
        ) {
          bullet.size() mustBe 1
          bullet.get(0).text() mustBe bulletContent
        }
      }
  }

  def testMustShowCorrectLinksAndCorrectContent(
      document: Document,
      linkCount: Int,
      content: List[String]
  )(using
      pos: Position
  ): Unit = {
    s"must have the correct number of bulletPoints " in {
      val p = document.getMainContent.getElementsByTag("a")
      p.size() mustBe linkCount
    }
    for linkContent <- content do
      s"must have links with content: '$linkContent'" in {

        val bullet = document.getMainContent.select(s"a:containsOwn($linkContent)")
        withClue(
          s"link with content '$linkContent' not found\n"
        ) {
          bullet.size() mustBe 1
          bullet.get(0).text() mustBe linkContent
        }
      }
  }

  def testMustShowCorrectHintsWithCorrectContent(
      document: Document,
      hintCount: Int,
      content: List[String]
  )(using pos: Position): Unit = {
    s"must have the correct number of hints " in {
      val hints = document.getMainContent.select("div.govuk-hint")
      hints.size() mustBe hintCount
    }
    for elementContent <- content do
      s"must have hints with content: '$elementContent'and" in {
        val elements = document.getMainContent.select(s"div.govuk-hint:containsOwn($elementContent)")
        withClue(
          s"Hints with content '$elementContent' not found\n"
        ) {
          elements.size() mustBe 1
          elements.get(0).text() mustBe elementContent
        }
      }
  }

  def testMustShowCorrectCaptionsWithCorrectContent(
      document: Document,
      captionCount: Int,
      content: List[String]
  )(using pos: Position): Unit = {
    s"must have the correct number of Captions" in {
      val captions = document.getMainContent.select("span.govuk-caption-m")
      captions.size() mustBe captionCount
    }
    for elementContent <- content do
      s"must have Captions with content: '$elementContent' " in {
        val elements = document.getMainContent.select(s"span.govuk-caption-m:containsOwn($elementContent)")
        withClue(
          s"Captions with content '$elementContent' not found\n"
        ) {
          elements.size() mustBe 1
          elements.get(0).text() mustBe elementContent
        }
      }
  }

  def testMustShowCorrectTaskLinksWithCorrectContent(
      document: Document,
      linkCount: Int,
      content: List[String]
  )(using pos: Position): Unit = {
    val classTag = "govuk-link govuk-task-list__link"
    s"must have the correct number of Captions" in {
      val links = document.getMainContent.getElementsByClass(classTag)
      links.size() mustBe linkCount
    }
    for elementContent <- content do
      s"must have task links with content: '$elementContent' " in {
        val elements = document.getMainContent.getElementsByClass(classTag)
        withClue(
          s"tasklinks with content '$elementContent' not found\n"
        ) {
          elements.size() mustBe 1
          elements.get(0).text() mustBe elementContent
        }
      }
  }

  def testMustShowCorrectInputsWithCorrectDefaultValues(
      document: Document,
      elementCount: Int,
      content: List[String]
  )(using pos: Position): Unit = {
    s"must have the correct number of inputs " in {
      val elements = document.getMainContent.select("input")
      elements.size() mustBe elementCount
    }
    for valueContent <- content do
      s"must have input with value: '$valueContent' " in {
        val inputs = document.getMainContent.select(s"input[value*=$valueContent]")
        withClue(
          s"Inputs with value '$valueContent' not found\n"
        ) {
          inputs.size() mustBe 1
          inputs.get(0).`val`() mustBe valueContent
        }
      }
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
