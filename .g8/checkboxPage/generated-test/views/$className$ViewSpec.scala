
package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.data.Form
import forms.$className$FormProvider
import models.$className$
import models.NormalMode
import pages.$className$Page
import views.html.$className$View
import views.$className$ViewSpec.*


class $className$ViewSpec extends ViewSpecBase[$className$View] {

  private val formProvider = new $className$FormProvider()
  lazy val form: Form[Set[$className$]]
  //private val messageKeyPrefix = "$className;format="decap"$"

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
    }
  }
}

object $className$ViewSpec {
  val pageHeading = "$className;format="decap"$"
  val pageTitle = "$className;format="decap"$"
}