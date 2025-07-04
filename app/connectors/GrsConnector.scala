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
import models.grs.create.NewJourneyRequest
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.writeableOf_JsValue
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import utils.FrontendHeaderCarrier

import java.net.URI
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GrsConnector @Inject (appConfig: FrontendAppConfig, http: HttpClientV2)(using
    ExecutionContext
) {

  def start(value: NewJourneyRequest)(using RequestHeader): Future[HttpResponse] = {
    given HeaderCarrier = FrontendHeaderCarrier(implicitly[RequestHeader])

    val url: String =
      if (appConfig.stubGrs) {
        s"${controllers.testonly.routes.GrsController.startGrs().absoluteURL()}"
      } else {
        s"${appConfig.grsBaseUrl}/incorporated-entity-identification/api/limited-company-journey"
      }

    http
      .post(new URI(url).toURL)(implicitly[HeaderCarrier])
      .withBody(Json.toJson(value))
      .execute[HttpResponse]
  }

  def retrieve(journeyId: String)(using RequestHeader): Future[HttpResponse] = {
    given HeaderCarrier = FrontendHeaderCarrier(implicitly[RequestHeader])
    val path            =
      if (appConfig.stubGrs) {
        s"${controllers.testonly.routes.GrsController.getGrs(journeyId).absoluteURL()}"
      } else {
        s"${appConfig.grsBaseUrl}/incorporated-entity-identification/api/journey/$journeyId"
      }

    http.get(URI(path).toURL).execute[HttpResponse]
  }

}
