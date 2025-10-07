
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
  private val form: Form[$className$] = formProvider()

  private def generateView(form: Form[$className$]): Document = {
    val view = SUT(form, NormalMode)
    Jsoup.parse(view.toString)
  }

  "$className$View" - {
    "when the form is empty (no errors)" - {
      val doc = generateView(form)
      doc.mustHaveCorrectPageTitle(pageHeading)
      doc.createTestForBackLink(show = true)
      doc.createTestMustHaveCorrectPageHeading(pageTitle)

      "must display the correct labels for fields" in {
        doc.select("label[for=$field1Name$]").text() mustBe field1Label
        doc.select("label[for=$field2Name$]").text() mustBe field2Label
      }
    }
  }
}

object $className$ViewSpec {
  val pageHeading = "$className$"
  val pageTitle = "$className$"
  val field1Label = "$field1Name$"
  val field2Label = "$field2Name$"
}
