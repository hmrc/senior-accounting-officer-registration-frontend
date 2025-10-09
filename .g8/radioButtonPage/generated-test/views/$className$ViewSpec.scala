
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

  private def generateView(form: Form[$className$], mode: Mode = NormalMode): Document = {
    val view = SUT(form, mode)
    Jsoup.parse(view.toString)
  }


  private def doChecks(doc:Document, mode:Mode): Unit = {
    doc.mustHaveCorrectPageTitle(pageHeading)
    doc.createTestForBackLink(show = true)
    doc.createTestMustHaveCorrectPageHeading(pageTitle)
    doc.createTestMustShowIsThisPageNotWorkingProperlyLink

    "must display the correct radio button lables" in {
      doc.getMainContent.select("label[for=value_0]").text() mustBe option1Label
      doc.getMainContent.select("label[for=value_1]").text() mustBe option2Label
    }

    doc.createTestMustHaveASubmissionButtonWhichSubmitsTo(
      controllers.routes.$className$Controller.onSubmit(mode),
      "Continue"
    )
  }

  "$className$View" - {
    "when using NormalMode, the form is empty (no errors)" - {
      val doc = generateView(form)
      doChecks(doc,NormalMode)
    }
    "when using CheckMode" - {
      val doc = generateView(form, CheckMode)
      doChecks(doc,CheckMode)
    }
  }
}

object $className$ViewSpec {
  val pageHeading = "$title$"
  val pageTitle = "$title$"
  val option1Label = "$option1msg$"
  val option2Label = "$option2msg$"
}
