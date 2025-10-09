
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

  private def doChecks(doc: Document, mode: Mode): Unit = {
    doc.mustHaveCorrectPageTitle(pageHeading)
    doc.createTestForBackLink(show = true)
    doc.createTestMustHaveCorrectPageHeading(pageTitle)
    doc.createTestMustShowIsThisPageNotWorkingProperlyLink

    "must display the correct labels for fields" in {
      doc.select("label[for=$field1Name$]").text() mustBe field1Label
      doc.select("label[for=$field2Name$]").text() mustBe field2Label
    }

    doc.createTestMustHaveASubmissionButtonWhichSubmitsTo(
      controllers.routes.$className$Controller.onSubmit(mode),
      "Continue"
    )
  }

  "$className$View" - {
    "when using NormalMode, when the form is empty (no errors)" - {
      val doc = generateView(form, NormalMode)
      doChecks(doc, NormalMode)
    }
    "when using CheckMode" - {
      val doc = generateView(form, CheckMode)
      doChecks(doc, CheckMode)
    }
  }
}

object $className$ViewSpec {
  val pageHeading = "$className;format="decap"$"
  val pageTitle = "$className;format="decap"$"
  val field1Label = "$field1Name$"
  val field2Label = "$field2Name$"
}
