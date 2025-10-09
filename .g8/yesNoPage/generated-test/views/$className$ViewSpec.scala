
package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.inject.Injector
import play.api.data.Form
import forms.$className$FormProvider
import models.{NormalMode, CheckMode, Mode}
import pages.$className$Page
import views.html.$className$View
import views.$className$ViewSpec.*


class $className$ViewSpec extends ViewSpecBase[$className$View] {

  private val formProvider = app.injector.instanceOf[$className$FormProvider]
  private val form: Form[Boolean] = formProvider()

  private def generateView(form: Form[Boolean], mode: Mode): Document = {
    val view = SUT(form, mode)
    Jsoup.parse(view.toString)
  }


  private def doChecks(doc: Document, mode: Mode): Unit = {
    doc.mustHaveCorrectPageTitle(pageHeading)
    doc.createTestForBackLink(show = true)
    doc.createTestMustHaveCorrectPageHeading(pageTitle)
    doc.createTestMustShowIsThisPageNotWorkingProperlyLink

    "must display the correct Yes or No labels" in {
      doc.getMainContent.select("label[for=value]").text() mustBe yesLabel
      doc.getMainContent.select("label[for=value-no]").text() mustBe noLabel
    }

    doc.createTestMustHaveASubmissionButtonWhichSubmitsTo(
      controllers.routes.$className$Controller.onSubmit(mode),
      "Continue"
    )
  }

  "$className$View" - {
    "when in NormalMode when the form is empty (no errors)" - {
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
  val yesLabel = "Yes"
  val noLabel = "No"
}
