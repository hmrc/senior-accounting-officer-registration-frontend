/*
 * Copyright 2026 HM Revenue & Customs
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
import play.api.http.Status.{NOT_FOUND, NO_CONTENT, OK}
import play.api.libs.json.JsObject
import play.api.libs.ws.writeableOf_JsValue
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.*
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import utils.FrontendHeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

import java.net.URI
import javax.inject.Inject

class EnrolmentStoreStubConnector @Inject() (
    appConfig: AppConfig,
    http: HttpClientV2
)(using ExecutionContext) {

  def upsertGroupPersona(groupId: String, persona: JsObject)(using request: RequestHeader): Future[Boolean] = {
    given HeaderCarrier = FrontendHeaderCarrier(request)

    groupExists(groupId).flatMap { exists =>
      val url =
        if exists then s"${appConfig.enrolmentStoreStubBaseUrl}/enrolment-store-stub/data/group/$groupId"
        else s"${appConfig.enrolmentStoreStubBaseUrl}/enrolment-store-stub/data"

      val response =
        if exists then {
          http.put(URI(url).toURL).withBody(persona).execute[HttpResponse]
        } else {
          http.post(URI(url).toURL).withBody(persona).execute[HttpResponse]
        }

      response.map(_.status == NO_CONTENT)
    }
  }

  private def groupExists(groupId: String)(using HeaderCarrier): Future[Boolean] =
    http
      .get(URI(s"${appConfig.enrolmentStoreStubBaseUrl}/enrolment-store-stub/data/group/$groupId").toURL)
      .execute[HttpResponse]
      .map {
        case response if response.status == OK        => true
        case response if response.status == NOT_FOUND => false
        case _                                        => false
      }
}
