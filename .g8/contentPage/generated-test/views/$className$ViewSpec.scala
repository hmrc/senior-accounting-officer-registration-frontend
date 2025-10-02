
package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import play.api.i18n.Messages
import views.html.$className$View

class $className$ViewSpec extends ViewSpecBase[$className$View] {

  private val messageKeyPrefix = "$className;format="decap"$"
  private val messageProvider: Messages = messages(app)
  private val pageHeading: String = messageProvider(messageKeyPrefix + ".heading")
  private val pageTitle: String = messageProvider(messageKeyPrefix + ".title")

  "$className$View" - {

    "must generate a view" - {
      val doc = Jsoup.parse(SUT().toString)
      doc.mustHaveCorrectPageTitle(pageHeading)
      doc.createTestMustHaveCorrectPageHeading(pageTitle)
      doc.createTestForBackLink(show = true)
    }

  }
}
