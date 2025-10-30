
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

  "$className$View" - {

    Mode.values.foreach { mode =>
      s"when using \$mode" - {
        "when the form is not filled in" - {
          val doc = generateView(form, mode)

          doc.createTestsWithStandardPageElements(
            pageTitle = pageTitle,
            pageHeading = pageHeading,
            showBackLink = true,
            showIsThisPageNotWorkingProperlyLink = true,
            hasError = false
          )

          doc.createTestsWithASingleTextInput(
            name = "value",
            label = pageHeading,
            value = "",
            hint = None
          )

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.$className$Controller.onSubmit(mode),
            buttonText = "Continue"
          )
        }

        "when the form is filled in" - {
          val doc = generateView(form.bind(Map("value" -> testInputValue)), mode)

          doc.createTestsWithStandardPageElements(
            pageTitle = pageTitle,
            pageHeading = pageHeading,
            showBackLink = true,
            showIsThisPageNotWorkingProperlyLink = true,
            hasError = false
          )

          doc.createTestsWithASingleTextInput(
            name = "value",
            label = pageHeading,
            value = testInputValue,
            hint = None
          )

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.$className$Controller.onSubmit(mode),
            buttonText = "Continue"
          )
        }

        "when the form has errors" - {
          val doc = generateView(form.withError("value", errorMessage), mode)

          doc.createTestsWithStandardPageElements(
            pageTitle = pageTitle,
            pageHeading = pageHeading,
            showBackLink = true,
            showIsThisPageNotWorkingProperlyLink = true,
            hasError = true
          )

          doc.createTestsWithASingleTextInput(
            name = "value",
            label = pageHeading,
            value = "",
            hint = None
          )

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.$className$Controller.onSubmit(mode),
            buttonText = "Continue"
          )

          doc.createTestsWithError(
            hasError = true,
            errorMessage = errorMessage
          )
        }
      }
    }
  }
}

object $className$ViewSpec {
  val pageHeading = "$className;format="decap"$"
  val pageTitle = "$className;format="decap"$"
  val testInputValue = "myTestInputValue"
  val errorMessage = "broken"
}
