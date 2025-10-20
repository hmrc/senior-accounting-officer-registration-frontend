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

package controllers

import config.FrontendAppConfig
import controllers.actions.*
import models.registration.CompanyDetails
import models.registration.RegistrationCompleteDetails
import pages.CompanyDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.RegistrationCompleteView

import java.time.{Clock, ZonedDateTime}
import javax.inject.Inject

class RegistrationCompleteController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    val controllerComponents: MessagesControllerComponents,
    view: RegistrationCompleteView,
    configuration: FrontendAppConfig,
    clock: Clock
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    request.userAnswers.get(CompanyDetailsPage) match
      case None                 => Redirect(routes.JourneyRecoveryController.onPageLoad())
      case Some(companyDetails) =>
        Ok(
          view(
            RegistrationCompleteDetails(
              companyName = companyDetails.companyName,
              registrationId = "XMPLR0123456789",
              registrationDateTime = ZonedDateTime.now(clock)
            ),
            configuration.hubUrl + "/senior-accounting-officer"
          )
        )
  }
}
