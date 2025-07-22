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

import connectors.GrsConnector
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models.UserAnswers
import models.grs.create.NewJourneyResponse
import models.grs.retrieve.CompanyDetails as GrsCompanyDetails
import models.registration.CompanyDetails
import pages.CompanyDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.GrsService
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.util.Try

class GrsController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    grsConnector: GrsConnector,
    val controllerComponents: MessagesControllerComponents,
    grsMappingService: GrsService,
    sessionRepository: SessionRepository
)(using ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def start(): Action[AnyContent] = identify.async { implicit request =>
    val grsStartRequest = grsMappingService.newRequest()

    for {
      r <- grsConnector.start(grsStartRequest)
    } yield {
      r.status match {
        case CREATED =>
          val response =
            Try(Json.parse(r.body).as[NewJourneyResponse])
              .getOrElse(throw InternalServerException("Malformatted start journey response from GRS"))
          SeeOther(response.journeyStartUrl)
        case code =>
          throw InternalServerException(s"Invalid start journey response from GRS, status=$code body=${r.body}")
      }
    }
  }

  def callBack(journeyId: String): Action[AnyContent] = (identify andThen getData) async { implicit request =>
    def convert(grsCompanyDetailsOrException: Either[Option[Exception], GrsCompanyDetails]): CompanyDetails = {
      for {
        grsCompanyDetails <- grsCompanyDetailsOrException
        companyDetails    <- grsMappingService.map(grsCompanyDetails)
      } yield companyDetails
    }.getOrElse(throw new InternalServerException("Invalid response from GRS"))

    for {
      grsCompanyDetailsOrException <- grsConnector.retrieve(journeyId)
      companyDetails = convert(grsCompanyDetailsOrException)
      updatedAnswer  = request.userAnswers
        .getOrElse(UserAnswers(request.userId))
        .set(CompanyDetailsPage, companyDetails)
        .get
      _ <- sessionRepository.set(updatedAnswer)
    } yield Redirect(routes.IndexController.onPageLoad())

  }

}
