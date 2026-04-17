
package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import views.html.NominatedCompanyDetailsGuidanceView
import views.NominatedCompanyDetailsGuidanceViewSpec.*

class NominatedCompanyDetailsGuidanceViewSpec extends ViewSpecBase[NominatedCompanyDetailsGuidanceView] {

  private def generateView(): Document = Jsoup.parse(SUT().toString)

  "NominatedCompanyDetailsGuidanceView" - {
    val doc: Document = generateView()

    doc.createTestsWithStandardPageElements(
      pageTitle = pageTitle,
      pageHeading = pageHeading,
      showBackLink = true,
      showIsThisPageNotWorkingProperlyLink = true,
      hasError = false
    )

    doc.createTestsWithOrWithoutError(hasError = false)
  }
}

object NominatedCompanyDetailsGuidanceViewSpec {
  val pageHeading = "nominatedCompanyDetailsGuidance"
  val pageTitle = "nominatedCompanyDetailsGuidance"
}
