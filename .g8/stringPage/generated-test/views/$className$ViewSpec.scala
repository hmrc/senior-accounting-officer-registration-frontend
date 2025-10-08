
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

  private def generateView(form: Form[String], mode: Mode = NormalMode): Document = {
    val view = SUT(form, mode)
    Jsoup.parse(view.toString)
  }

  "$className$View" - {
    "when the form is empty (no errors), NormalMode" - {
      val doc = generateView(form)
      doc.mustHaveCorrectPageTitle(pageHeading)
      doc.createTestForBackLink(show = true)
      doc.createTestMustHaveCorrectPageHeading(pageTitle)
      doc.createTestMustShowIsThisPageNotWorkingProperlyLink

      "must display the correct label" in {
        doc.select("label[for=value]").text() mustBe pageTitle
      }

      doc.createTestMustHaveASubmissionButtonWhichSubmitsTo(
        controllers.routes.$className$Controller.onSubmit(NormalMode),
        "Continue"
      )
    }
    "when using CheckMode" - {
      val doc = generateView(form, CheckMode)

      doc.createTestMustHaveASubmissionButtonWhichSubmitsTo(
        controllers.routes.$className$Controller.onSubmit(CheckMode),
        "Continue"
      )
    }
  }
}

object $className$ViewSpec {
  val pageHeading = "$className;format="decap"$"
  val pageTitle = "$className;format="decap"$"
}

