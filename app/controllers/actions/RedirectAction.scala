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

package controllers.actions

import scala.concurrent.Future
import models.requests.DataRequest
import javax.inject.Inject
import repositories.SessionRepository
import scala.concurrent.ExecutionContext

import play.api.mvc.ActionFilter
import play.api.mvc.Results.{Redirect}
import play.api.mvc.Result

import models.ContactHaveYouAddedAll
import controllers.routes
import pages.ContactHaveYouAddedAllPage
import models.ContactType

class RedirectActionImpl @Inject() (
)(implicit val executionContext: ExecutionContext) extends RedirectAction {
  override protected def filter[A](request: DataRequest[A]): Future[Option[Result]] = {
    request.userAnswers.get(ContactHaveYouAddedAllPage(ContactType.First)) match {
      case Some(ContactHaveYouAddedAll.Yes) => Future.successful(Some(Redirect(routes.IndexController.onPageLoad())))
      case _ => Future.successful(None)
      // case _ => for {
      //   storedData <- sessionRepository.get(request.userId)
      //   _ <- storedData match {
      //     case Some(data) if data.get(ContactHaveYouAddedAllPage(ContactType.First)).contains(ContactHaveYouAddedAll.Yes) =>
      //       Future.successful(Some(Redirect(routes.IndexController.onPageLoad())))
      //     case _ =>
      //       Future.successful(None)
      //   }
      // } yield {}
    }
  }
}
trait RedirectAction extends ActionFilter[DataRequest]

