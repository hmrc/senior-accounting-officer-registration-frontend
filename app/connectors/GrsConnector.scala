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

package connectors

import config.FrontendAppConfig
import connectors.GrsConnector.given
import models.grs.create.NewJourneyRequest
import models.grs.retrieve.CompanyDetails
import play.api.http.Status.*
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.writeableOf_JsValue
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.*
import uk.gov.hmrc.http.client.HttpClientV2
import utils.FrontendHeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

import java.net.URI
import javax.inject.Inject

class GrsConnector @Inject (appConfig: FrontendAppConfig, http: HttpClientV2)(using
    ExecutionContext
) {

  def start(value: NewJourneyRequest)(using RequestHeader): Future[HttpResponse] = {
    given HeaderCarrier = FrontendHeaderCarrier(implicitly[RequestHeader])

    val url: String =
      if appConfig.stubGrs then {
        s"${appConfig.grsStubsBaseUrl}${_root_.controllers.testonly.routes.GrsStubsController.startGrs()}"
      } else {
        s"${appConfig.grsBaseUrl}/incorporated-entity-identification/api/limited-company-journey"
      }

    http
      .post(new URI(url).toURL)(implicitly[HeaderCarrier])
      .withBody(Json.toJson(value))
      .execute[HttpResponse]
  }

  def retrieve(journeyId: String)(using RequestHeader): Future[Either[Option[Exception], CompanyDetails]] = {
    given HeaderCarrier = FrontendHeaderCarrier(implicitly[RequestHeader])
    val path            =
      if appConfig.stubGrs then {
        s"${appConfig.grsStubsBaseUrl}${_root_.controllers.testonly.routes.GrsStubsController.getGrs(journeyId).url()}"
      } else {
        s"${appConfig.grsBaseUrl}/incorporated-entity-identification/api/journey/$journeyId"
      }
    http.get(URI(path).toURL).execute[Either[Option[Exception], CompanyDetails]]
  }

}

object GrsConnector {
  given HttpReads[Either[Option[Exception], CompanyDetails]] =
    (method: String, url: String, response: HttpResponse) =>
      response.status match {
        case OK =>
          (for {
            json      <- Try(response.json).toEither
            validated <- json.validate[CompanyDetails].asEither
          } yield validated).left
            .map(_ => Some(new InternalServerException("Retrieve from GRS failed with invalid body")))

        case NOT_FOUND => Left(None)
        case status    =>
          Left(Some(new InternalServerException(s"Retrieve from GRS failed with status: $status")))
      }
}
