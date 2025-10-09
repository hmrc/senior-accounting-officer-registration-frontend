
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

  private def generateView(form: Form[String], mode: Mode, bound: Boolean): Document = {
    val view = bound match {
      case true => SUT(form.bind(Map("value" -> testInputValue)), mode)
      case _ => SUT(form, mode)
    }
    Jsoup.parse(view.toString)
  }


  private def doInputChecks(doc: Document, mode: Mode, boundValue: Boolean): Unit = {
    doc.createTestMustShowASingleInput(
      expectedLabel = pageHeading,
      expectedValue = if(boundValue == true) testInputValue else "",
      expectedHint = None
    )
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
    "when using NormalMode, with bound form" - {
      val doc = generateView(form, NormalMode, false)
      doChecks(doc, NormalMode)
      val docWithBoundFormValue = generateView(form, NormalMode, true)
      doInputChecks(docWithBoundFormValue, NormalMode, true)
    }
    "when using CheckMode" - {
      val doc = generateView(form, CheckMode, false)
      doChecks(generateView(form, CheckMode, false), CheckMode)
      val docWithBoundFormValue = generateView(form, CheckMode, true)
      doInputChecks(docWithBoundFormValue, CheckMode, true)
    }
  }
}

object $className$ViewSpec {
  val pageHeading = "$className;format="decap"$"
  val pageTitle = "$className;format="decap"$"
  val testInputValue = "myTestInputValue"
}
