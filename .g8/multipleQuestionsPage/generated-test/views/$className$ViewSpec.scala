
package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.inject.Injector
import play.api.data.Form
import forms.$className$FormProvider
import models.$className$
import models.{NormalMode, CheckMode, Mode}
import pages.$className$Page
import views.html.$className$View
import views.$className$ViewSpec.*


class $className$ViewSpec extends ViewSpecBase[$className$View] {

  private val formProvider = app.injector.instanceOf[$className$FormProvider]
  private val form: Form[$className$] = formProvider()

  private def generateView(form: Form[$className$], mode: Mode): Document = {
    val view = SUT(form, mode)
    Jsoup.parse(view.toString)
  }

  private def doCommonChecks(doc: Document, mode: Mode): Unit = {
    doc.mustHaveCorrectPageTitle(pageHeading)
    doc.createTestForBackLink(show = true)
    doc.createTestMustHaveCorrectPageHeading(pageTitle)
    doc.createTestMustShowIsThisPageNotWorkingProperlyLink

    doc.createTestMustHaveASubmissionButtonWhichSubmitsTo(
      controllers.routes.$className$Controller.onSubmit(mode),
      "Continue"
    )
  }

  "$className$View" - {
    Mode.values.foreach { mode =>
      s"when using \$mode" - {
        "when using unBound form" - {
          val doc = generateView(form, mode)

          doCommonChecks(doc, mode)
          doc.createTestMustShowInput(expectedName = "$field1Name$", expectedLabel = field1Label, expectedValue = "")
          doc.createTestMustShowInput(expectedName = "$field2Name$", expectedLabel = field2Label, expectedValue = "")
        }

        "when using bound form" - {
          val boundForm = form.bind(Map("$field1Name$" -> testInputValue1, "$field2Name$" -> testInputValue2))
          val doc = generateView(boundForm, mode)

          doCommonChecks(doc, mode)
          doc.createTestMustShowInput(expectedName = "$field1Name$", expectedLabel = field1Label, expectedValue = testInputValue1)
          doc.createTestMustShowInput(expectedName = "$field2Name$", expectedLabel = field2Label, expectedValue = testInputValue2)
        }
      }
    }
  }
}

object $className$ViewSpec {
  val pageHeading = "$className;format="decap"$"
  val pageTitle = "$className;format="decap"$"
  val field1Label = "$field1Name$"
  val field2Label = "$field2Name$"

  val testInputValue1 = "test value 1"
  val testInputValue2 = "test value 2"

}
