/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package services

import config.FrontendAppConfig
import models.grs.create.{NewJourneyRequest, ServiceLabels}
import models.grs.retrieve.{CompanyDetails as GrsCompanyDetails, Registration}
import models.registration.CompanyDetails
import play.api.i18n.{Lang, MessagesApi}
import play.api.mvc.Request
import uk.gov.hmrc.hmrcfrontend.config.AccessibilityStatementConfig
import uk.gov.hmrc.http.InternalServerException

import java.net.URI
import javax.inject.Inject

class GrsService @Inject() (
    appConfig: FrontendAppConfig,
    accessibilityStatementConfig: AccessibilityStatementConfig,
    messagesApi: MessagesApi
) {

  def newRequest()(using request: Request[?]): NewJourneyRequest = {
    val continueUrl =
      appConfig.prependHost(controllers.routes.GrsController.callBack("").url().replaceAll("\\?.*$", ""))
    val request = NewJourneyRequest(
      continueUrl = continueUrl,
      businessVerificationCheck = false,
      deskProServiceId = appConfig.contactFormServiceIdentifier,
      signOutUrl = appConfig.prependHost(controllers.auth.routes.AuthController.signOut()),
      regime = "VATC", // TODO confirm
      accessibilityUrl = accessibilityStatementConfig.url.get,
      labels = ServiceLabels(en = messagesApi.preferred(Seq(Lang("en"))).messages("service.name"))
    )

    request match {
      case r if appConfig.grsAllowsRelativeUrl =>
        // GRS only permits relative URLs locally
        r
      case r =>
        r.copy(
          continueUrl = toRelativeUrl(r.continueUrl),
          signOutUrl = toRelativeUrl(r.signOutUrl),
          accessibilityUrl = toRelativeUrl(r.accessibilityUrl)
        )
    }
  }

  private[services] def toRelativeUrl(url: String): String = {
    val uri = new URI(url)
    if Option(uri.getRawQuery).isDefined then s"${uri.getPath}?${uri.getRawQuery}"
    else uri.getPath
  }

  def map(companyDetails: GrsCompanyDetails): Either[Exception, CompanyDetails] =
    companyDetails.registration match {
      case Registration.Registered(id) =>
        Right(
          CompanyDetails(
            companyName = companyDetails.companyProfile.companyName,
            companyNumber = companyDetails.companyProfile.companyNumber,
            ctUtr = companyDetails.ctutr,
            registeredBusinessPartnerId = id
          )
        )
      case _ => Left(new InternalServerException("Unable to convert company details"))
    }

}
