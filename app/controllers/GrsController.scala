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

import cats.data.EitherT
import connectors.GrsConnector
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models.UserAnswers
import models.grs.create.NewJourneyResponse
import models.registration.CompanyDetails
import pages.CompanyDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.*
import repositories.SessionRepository
import services.GrsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

import javax.inject.Inject

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
          Try(Json.parse(r.body).as[NewJourneyResponse]).toEither
            .map(response => SeeOther(response.journeyStartUrl))
            .left
            .map(_ => InternalServerError("Malformatted start journey response from GRS"))
            .merge
        case code =>
          InternalServerError(
            s"Invalid start journey response from GRS, status=$code body=${r.body}, requestBody=${Json.toJson(grsStartRequest)}"
          )
      }
    }
  }

  def callBack(journeyId: String): Action[AnyContent] = (identify andThen getData) async { implicit request =>
    val flow: EitherT[Future, Result, Result] = for {
      grsCompanyDetails <- EitherT(grsConnector.retrieve(journeyId))
        .leftMap(_ => InternalServerError("Failure response from GRS"))
      companyDetails <- EitherT
        .fromEither[Future](grsMappingService.map(grsCompanyDetails))
        .leftMap(_ => InternalServerError("Invalid data from GRS"))
      updatedAnswer <- EitherT
        .fromEither[Future](
          request.userAnswers
            .getOrElse(UserAnswers(request.userId))
            .set(CompanyDetailsPage, companyDetails)
            .toEither
        )
        .leftMap(_ => InternalServerError("Failed to set Session data"))
      _ <- EitherT
        .right(sessionRepository.set(updatedAnswer))
        .leftMap(_ => InternalServerError("Failed to update Session repository"))
    } yield Redirect(routes.IndexController.onPageLoad())

    flow.merge
  }

}
