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

import config.AppConfig
import connectors.EtmpSubscriptionConnector.given
import models.signup.{EtmpSubscriptionRequest, EtmpSubscriptionResponse}
import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.libs.ws.writeableOf_JsValue
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.*
import uk.gov.hmrc.http.client.HttpClientV2
import utils.FrontendHeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

import java.net.URI
import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant}
import java.util.UUID
import javax.inject.Inject

class EtmpSubscriptionConnector @Inject() (
    appConfig: AppConfig,
    http: HttpClientV2,
    clock: Clock
)(using
    ExecutionContext
) {

  def subscribe(
      request: EtmpSubscriptionRequest
  )(using requestHeader: RequestHeader): Future[Either[Exception, EtmpSubscriptionResponse]] = {
    given HeaderCarrier = FrontendHeaderCarrier(requestHeader)

    http
      .post(URI(appConfig.etmpSubscriptionUrl).toURL)
      .setHeader(headers*)
      .withBody(Json.toJson(request))
      .execute[Either[Exception, EtmpSubscriptionResponse]]
      .recover { case exception: Exception =>
        Left(exception)
      }
  }

  private def headers: Seq[(String, String)] =
    Seq(
      "Content-Type"          -> "application/json",
      "Authorization"         -> s"Basic ${appConfig.etmpSubscriptionAuthorizationToken}",
      "X-Transmitting-System" -> "HIP",
      "X-Originating-System"  -> "MDTP",
      "correlationid"         -> UUID.randomUUID().toString,
      "X-Receipt-Date"        -> Instant.now(clock).truncatedTo(ChronoUnit.SECONDS).toString
    )
}

object EtmpSubscriptionConnector {

  given HttpReads[Either[Exception, EtmpSubscriptionResponse]] =
    (method: String, url: String, response: HttpResponse) =>
      response.status match {
        case CREATED =>
          (for {
            json      <- Try(response.json).toEither
            validated <- json.validate[EtmpSubscriptionResponse].asEither
          } yield validated).left
            .map(_ => new InternalServerException("ETMP subscription returned invalid body"))
        case status =>
          Left(new InternalServerException(s"ETMP subscription failed with status: $status"))
      }
}
