
package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.inject.Injector
import play.api.data.Form
import models.NormalMode
import pages.$className$Page
import forms.$className$FormProvider
import views.html.$className$View
import views.$className$ViewSpec.*
import models.Mode


class $className$ViewSpec extends ViewSpecBase[$className$View] {

  private val formProvider = app.injector.instanceOf[$className$FormProvider]
  private val form: Form[Int] = formProvider()

  private def generateView(form: Form[Int], mode: Mode): Document = {
    val view = SUT(form, mode)
    Jsoup.parse(view.toString)
  }

  "$className$View" - {
    Mode.values.foreach { mode =>
      s"when using \$mode" - {
        "when the form is not filled in" - {
          val doc = generateView(form, mode)
          doc.mustHaveCorrectPageTitle(pageHeading)
          doc.createTestForBackLink(show = true)
          doc.createTestMustHaveCorrectPageHeading(pageTitle)
          doc.createTestMustShowIsThisPageNotWorkingProperlyLink

          doc.createTestMustShowASingleInput(
            expectedName = "value",
            expectedLabel = "$className$",
            expectedValue = "",
            expectedHint = None
          )

          doc.createTestMustHaveASubmissionButtonWhichSubmitsTo(
            controllers.routes.$className$Controller.onSubmit(mode),
            "Continue"
          )
        }

        "when the form is filled in" - {
          val doc = generateView(form.bind(Map("value" -> testInputValue)), mode)
          doc.mustHaveCorrectPageTitle(pageHeading)
          doc.createTestForBackLink(show = true)
          doc.createTestMustHaveCorrectPageHeading(pageTitle)
          doc.createTestMustShowIsThisPageNotWorkingProperlyLink

          doc.createTestMustShowASingleInput(
            expectedName = "value",
            expectedLabel = "$className$",
            expectedValue = testInputValue,
            expectedHint = None
          )

          doc.createTestMustHaveASubmissionButtonWhichSubmitsTo(
            controllers.routes.$className$Controller.onSubmit(mode),
            "Continue"
          )
        }
      }
    }
  }
}


object $className$ViewSpec {
  val pageHeading = "$className$"
  val pageTitle = "$className$"
  val testInputValue = $minimum$.toString
}
