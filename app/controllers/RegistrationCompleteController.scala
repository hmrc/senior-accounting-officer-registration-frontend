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

import controllers.actions.*
import models.registration.RegistrationCompleteDetails
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.RegistrationCompleteView

import java.time.{LocalDateTime, ZoneOffset, ZonedDateTime}
import javax.inject.Inject
import repositories.SessionRepository
import scala.concurrent.ExecutionContext
import pages.CompanyDetailsPage

class RegistrationCompleteController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: RegistrationCompleteView,
  sessionRepository: SessionRepository,
)(using ec: ExecutionContext) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    sessionRepository.get(request.userAnswers.id).map{
      option => {
        option match
          case None => {        // TODO: how to handle this situation?
            val registrationCompleteDetails = RegistrationCompleteDetails(
              companyName = "Boom",
              registrationId = "XMPLR0123456789",
              registrationDateTime = ZonedDateTime.of(LocalDateTime.of(2025, 1, 17, 11, 45), ZoneOffset.UTC)
            )

            Ok(view(registrationCompleteDetails))
          }
          case Some(userAnswers) => {
            val companyName = userAnswers.get(CompanyDetailsPage) match 
              case Some(x) => x.companyName
              case None => "Bang" // TODO: how to handle this situation?

            val registrationCompleteDetails = RegistrationCompleteDetails(
              companyName = companyName,
              registrationId = "XMPLR0123456789",
              registrationDateTime = ZonedDateTime.of(LocalDateTime.of(2025, 1, 17, 11, 45), ZoneOffset.UTC)
            )

            Ok(view(registrationCompleteDetails))
        }
      }
        
    }
    
  }
}
