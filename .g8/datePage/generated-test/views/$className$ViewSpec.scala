
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

          "must display the correct label" in {
            doc.select("label[for=value.day]").text() mustBe "Day"
            doc.select("label[for=value.month]").text() mustBe "Month"
            doc.select("label[for=value.year]").text() mustBe "Year"
          }

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.$className$Controller.onSubmit(mode),
            buttonText = "Continue"
          )
        }

        "when the form is filled in" - {
          val doc = generateView(form.bind(Map("value.day" -> "1", "value.month" -> "1", "value.year" -> "2000")), mode)

          doc.createTestsWithStandardPageElements(
            pageTitle = pageTitle,
            pageHeading = pageHeading,
            showBackLink = true,
            showIsThisPageNotWorkingProperlyLink = true,
            hasError = false
          )

          "must display the correct label" in {
            doc.select("label[for=value.day]").text() mustBe "Day"
            doc.select("label[for=value.month]").text() mustBe "Month"
            doc.select("label[for=value.year]").text() mustBe "Year"
          }

          doc.createTestsWithSubmissionButton(
            action = controllers.routes.$className$Controller.onSubmit(mode),
            buttonText = "Continue"
          )
        }
      }
    }
  }
}


object $className$ViewSpec {
  val pageHeading = "$className$"
  val pageTitle = "$className$"
}
