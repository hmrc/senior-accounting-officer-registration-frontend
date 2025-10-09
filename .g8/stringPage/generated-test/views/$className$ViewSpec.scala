
package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.inject.Injector
import play.api.data.Form
import models.{NormalMode, CheckMode, Mode}
import pages.$className$Page
import forms.$className$FormProvider
import views.html.$className$View
import views.$className$ViewSpec.*


class $className$ViewSpec extends ViewSpecBase[$className$View] {

  private val formProvider = app.injector.instanceOf[$className$FormProvider]
  private val form: Form[String] = formProvider()

  private def generateView(form: Form[String], mode: Mode): Document = {
    val view = SUT(form, mode)
    Jsoup.parse(view.toString)
  }

  private def doChecks(doc: Document, mode: Mode): Unit = {
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
    "when using NormalMode" - {
      val doc = generateView(form, NormalMode)
      doChecks(doc, NormalMode)

      "when using unBound form" - {
        doc.createTestMustShowASingleInput(
          expectedLabel = pageHeading,
          expectedValue = "",
          expectedHint = None
        )
      }
      val docWithBoundForm = generateView(form.bind(Map("value" -> testInputValue)), NormalMode)
      "when using bound form" - {
        docWithBoundForm.createTestMustShowASingleInput(
          expectedLabel = pageHeading,
          expectedValue = testInputValue,
          expectedHint = None
        )
      }
    }

    "when using CheckMode" - {

      val doc = generateView(form, CheckMode)
      doChecks(doc, CheckMode)

      "when using unBound form" - {
        doc.createTestMustShowASingleInput(
          expectedLabel = pageHeading,
          expectedValue = "",
          expectedHint = None
        )
      }

      val docWithBoundForm = generateView(form.bind(Map("value" -> testInputValue)), CheckMode)
      "when using bound form" - {
        docWithBoundForm.createTestMustShowASingleInput(
          expectedLabel = pageHeading,
          expectedValue = testInputValue,
          expectedHint = None
        )
      }
    }
  }
}

object $className$ViewSpec {
  val pageHeading = "$className;format="decap"$"
  val pageTitle = "$className;format="decap"$"
  val testInputValue = "myTestInputValue"
}
