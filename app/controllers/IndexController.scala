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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DashboardView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class IndexController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    bradsService: BradsService,
    val controllerComponents: MessagesControllerComponents,
    view: DashboardView
)(using ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData) { implicit request =>
    Ok(view()) // we want to have the view generate this for each one. 
    // IDM it being like an argument passed to the view
  }

  def continue: Action[AnyContent] = (identify andThen getData) { implicit request =>
    Redirect(routes.CheckYourAnswersController.onPageLoad())
  }

}

import models.UserAnswers
import pages.ContactsPage

class BradsService {
  def getStatus(userAnswers: UserAnswers): Option[Map[String, Boolean]] = {
    userAnswers.get(ContactsPage).collect({case contacts if contacts.nonEmpty =>
      Map(
        "companyDetails" -> true,
        "contactDetails" -> false,
        "checkYourAnswers" -> false
    )})
      
  }
}