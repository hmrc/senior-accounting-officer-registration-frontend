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
import forms.ContactCheckYourAnswersFormProvider

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ContactCheckYourAnswersView
import services.ContactCheckYourAnswersService
import models.ContactInfo
import pages.ContactsPage
import repositories.SessionRepository
import scala.concurrent.{Future, ExecutionContext}

class ContactCheckYourAnswersController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: ContactCheckYourAnswersFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: ContactCheckYourAnswersView,
    service: ContactCheckYourAnswersService,
    sessionRepository: SessionRepository
)(using ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val contactInfos = service.getContactInfos(request.userAnswers)
    if contactInfos.isEmpty
    then Redirect(routes.JourneyRecoveryController.onPageLoad())
    else Ok(view(contactInfos))
  }

  def saveAndContinue: Action[AnyContent] = (identify andThen getData andThen requireData) async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        _ => Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad())),
        dataReceived =>
          for {
            compare <- Future.successful(service.getContactInfos(request.userAnswers) == dataReceived)
            // only commit when the data in the form matches the data found in repo
            response <- if (compare) Future
              .fromTry(request.userAnswers.set(ContactsPage, dataReceived))
              .flatMap(sessionRepository.set)
              .map(_ => Redirect(routes.IndexController.onPageLoad())) 
              else Future.failed(???)
          } yield { response }
      )
  }
}
