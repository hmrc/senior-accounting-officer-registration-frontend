
package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.data.Form
import models.NormalMode
import pages.$className$Page
import forms.$className$FormProvider
import views.html.$className$View
import views.$className$ViewSpec.*


class $className$ViewSpec extends ViewSpecBase[$className$View] {

  private val formProvider = new $className$FormProvider()
  lazy val form: Form[Int] = formProvider()

  private def generateView(form: Form[Int]): Document = {
    val view = SUT(form, NormalMode)
    Jsoup.parse(view.toString)
  }

  "$className$View" - {
    "when the form is empty (no errors)" - {
      val doc = generateView(form)
      doc.mustHaveCorrectPageTitle(pageHeading)
      doc.createTestForBackLink(show = true)
      doc.createTestMustHaveCorrectPageHeading(pageTitle)

      "must display the correct label" in {
        doc.select("label[for=value]").text() mustBe pageTitle
      }
    }
  }
}


object $className$ViewSpec {
  val pageHeading = "$classN}