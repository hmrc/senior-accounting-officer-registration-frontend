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

package controllers.testonly

import config.AppConfig
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.*
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.testonly.SendEmailView

import scala.concurrent.{ExecutionContext, Future}

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

class SendEmailController @Inject() (
    override val messagesApi: MessagesApi,
    sendEmailView: SendEmailView,
    val controllerComponents: MessagesControllerComponents,
    httpClient: HttpClientV2,
    appConfig: AppConfig
)(using ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def get: Action[AnyContent] = Action { implicit request =>
    Ok(sendEmailView())
  }

  def send: Action[Map[String, Seq[String]]] = Action(parse.formUrlEncoded).async { implicit request =>
    given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    field("email") match {
      case None        => Future.successful(BadRequest(sendEmailView(Some("Enter an email address."))))
      case Some(email) =>
        val params    = Seq("email" -> email) ++ field("name").map("name" -> _)
        val query     = params.map((key, value) => s"$key=${encode(value)}").mkString("&")
        val targetUrl = s"${appConfig.protectedServiceBaseUrl}/test-only/send-email?$query"

        httpClient
          .get(url"$targetUrl")
          .execute[HttpResponse]
          .map(response => Ok(sendEmailView(Some(describe(response)))))
    }
  }

  private def field(key: String)(using request: Request[Map[String, Seq[String]]]): Option[String] =
    request.body.get(key).flatMap(_.headOption).map(_.trim).filter(_.nonEmpty)

  private def encode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)

  private def describe(response: HttpResponse): String =
    if response.status == ACCEPTED then s"Sent. ${response.body}"
    else s"Failed (${response.status}). ${response.body}"
}
