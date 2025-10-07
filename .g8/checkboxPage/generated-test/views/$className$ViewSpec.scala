
package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.inject.Injector
import play.api.data.Form
import forms.$className$FormProvider
import models.$className$
import models.NormalMode
import pages.$className$Page
import views.html.$className$View
import views.$className$ViewSpec.*


class $className$ViewSpec extends ViewSpecBase[$className$View] {

  private val formProvider = app.injector.instanceOf[$className$FormProvider]
  private val form: Form[Set[$className$]] = formProvider()

  private def generateView(form: Form[Set[$className$]]): Document = {
    val view = SUT(form, NormalMode)
    Jsoup.parse(view.toString)
  }

  "$className$View" - {
    "when the form is empty (no errors)" - {
      val doc = generateView(form)
      doc.mustHaveCorrectPageTitle(pageHeading)
      doc.createTestForBackLink(show = true)
      doc.createTestMustHaveCorrectPageHeading(pageTitle)

      "must display the correct checkbox labels" in {
        doc.select("label[for=value_0]").text() mustBe option1Label
        doc.select("label[for=value_1]").text() mustBe option2Label
      }
    }
  }
}

object $className$ViewSpec {
  val pageHeading = "$title$"
  val pageTitle = "$title$"
  val option1Label = "$option1msg$"
  val option2Label = "$option2msg$"
}
