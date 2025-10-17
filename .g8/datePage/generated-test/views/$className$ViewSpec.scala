
package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.inject.Injector
import models.{NormalMode, CheckMode, Mode}
import pages.$className$Page
import forms.$className$FormProvider
import views.html.$className$View
import views.$className$ViewSpec.*
import java.time.LocalDate


class $className$ViewSpec extends ViewSpecBase[$className$View] {

  private val formProvider = app.injector.instanceOf[$className$FormProvider]
  private val form: Form[LocalDate] = formProvider()

  private def generateView(form: Form[LocalDate], mode: Mode): Document = {
    val view = SUT(form, mode)
    Jsoup.parse(view.toString)
  }

  private def doChecks(doc: Document, mode: Mode): Unit = {
    doc.mustHaveCorrectPageTitle(pageHeading)
    doc.createTestForBackLink(show = true)
    doc.createTestMustHaveCorrectPageHeading(pageTitle)
    doc.createTestMustShowIsThisPageNotWorkingProperlyLink

    "must display the correct label" in {
      doc.select("label[for=value.day]").text() mustBe "Day"
      doc.select("label[for=value.month]").text() mustBe "Month"
      doc.select("label[for=value.year]").text() mustBe "Year"
    }

    doc.createTestMustHaveASubmissionButtonWhichSubmitsTo(
      controllers.routes.$className$Controller.onSubmit(mode),
      "Continue"
    )
  }

  "$className$View" - {
    "when in NormalMode, the form is empty (no errors)" - {
      val doc = generateView(form, NormalMode)
      doChecks(doc, NormalMode)
    }

    "when in CheckMode" - {
      val doc = generateView(form, CheckMode)
      doChecks(doc, CheckMode)
    }
  }
}


object $className$ViewSpec {
  val pageHeading = "$className$"
  val pageTitle = "$className$"
}
