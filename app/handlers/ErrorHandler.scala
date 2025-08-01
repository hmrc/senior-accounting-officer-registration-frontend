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

package handlers

import play.api.{Logger, PlayException}

import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{RequestHeader, Result}
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler
import views.html.ErrorTemplate

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ErrorHandler @Inject() (
    val messagesApi: MessagesApi,
    view: ErrorTemplate
)(override implicit val ec: ExecutionContext)
    extends FrontendErrorHandler
    with I18nSupport {

  private val logger = Logger(getClass)

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit
      rh: RequestHeader
  ): Future[Html] =
    Future.successful(view(pageTitle, heading, message))

  // copied from FrontendErrorHandler in order to override the private logError method
  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    logError(request, exception)
    resolveError(request, exception)
  }

  // copied from FrontendErrorHandler (the only intended change is from logger.error to logger.warn)
  private def logError(request: RequestHeader, ex: Throwable): Unit =
    logger.warn(
      """
        |
        |! %sInternal server error, for (%s) [%s] ->
        | """.stripMargin
        .format(
          ex match {
            case p: PlayException => "@" + p.id + " - "
            case _                => ""
          },
          request.method,
          request.uri
        ),
      ex
    )

}
