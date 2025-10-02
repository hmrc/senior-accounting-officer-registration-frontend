
package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import play.api.i18n.Messages
import views.html.$className$View
import views.$className$ViewSpec.*

class $className$ViewSpec extends ViewSpecBase[$className$View] {


  "$className$View" - {

    "must generate a view" - {
      val doc = Jsoup.parse(SUT().toString)
      doc.mustHaveCorrectPageTitle(pageHeading)
      doc.createTestMustHaveCorrectPageHeading(pageTitle)
      doc.createTestForBackLink(show = true)
    }
  }
}

object $className$ViewSpec {
  val pageHeading = "$className;format="decap"$"
  val pageTitle = "$className;format="decap"$"
}