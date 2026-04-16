
package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.inject.Injector
import play.api.data.Form
import models.{NormalMode, CheckMode, Mode}
import pages.NominatedCompanyDetailsGuidancePage
import forms.NominatedCompanyDetailsGuidanceFormProvider
import views.html.NominatedCompanyDetailsGuidanceView
import views.NominatedCompanyDetailsGuidanceViewSpec.*


class NominatedCompanyDetailsGuidanceViewSpec extends ViewSpecBase[NominatedCompanyDetailsGuidanceView] {

  private val formProvider = app.injector.instanceOf[NominatedCompanyDetailsGuidanceFormProvider]
  private val form: Form[String] = formProvider()

  private def generateView(form: Form[String], mode: Mode): Document = {
    val view = SUT(form, mode)
    Jsoup.parse(view.toString)
  }

  "NominatedCompanyDetailsGuidanceView" - {

    Mode.values.foreach { mode =>
      s"when using $mode" - {
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
            hint = None,
            hasError = false
          )

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.NominatedCompanyDetailsGuidanceController.onSubmit(mode),
            buttonText = "Continue"
          )

          doc.createTestsWithOrWithoutError(
            hasError = false
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
            hint = None,
            hasError = false
          )

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.NominatedCompanyDetailsGuidanceController.onSubmit(mode),
            buttonText = "Continue"
          )

          doc.createTestsWithOrWithoutError(
            hasError = false
          )
        }

        "when the form has errors" - {
          val doc = generateView(form.withError("value", "broken"), mode)

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
            hint = None,
            hasError = true
          )

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.NominatedCompanyDetailsGuidanceController.onSubmit(mode),
            buttonText = "Continue"
          )

          doc.createTestsWithOrWithoutError(
            hasError = true
          )
        }
      }
    }
  }
}

object NominatedCompanyDetailsGuidanceViewSpec {
  val pageHeading = "nominatedCompanyDetailsGuidance"
  val pageTitle = "nominatedCompanyDetailsGuidance"
  val testInputValue = "myTestInputValue"
}
