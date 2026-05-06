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
import models.{ContactInfo, ContactType, NormalMode}
import navigation.Navigator
import pages.{ContactCheckYourAnswersPage, ContactsPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.ContactCheckYourAnswersService
import uk.gov.hmrc.http.BadRequestException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ContactCheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}
import javax.inject.Inject

class ContactCheckYourAnswersController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: ContactCheckYourAnswersFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: ContactCheckYourAnswersView,
    service: ContactCheckYourAnswersService,
    sessionRepository: SessionRepository,
    navigator: Navigator
)(using ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[ContactInfo] = formProvider()

  def onPageLoad(contactType: ContactType): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      println("Inside onPageLoad")
      service.getContactInfo(request.userAnswers, contactType) match {
        case Some(answers) => Ok(view(answers, contactType))
        case None          => Redirect(routes.JourneyRecoveryController.onPageLoad())
      }
  }

  def saveAndContinue(contactType: ContactType): Action[AnyContent] =
    (identify andThen getData andThen requireData) async { implicit request =>
      service.getContactInfo(request.userAnswers, contactType) match {
        case Some(contactInfo) =>
          val contacts: List[ContactInfo] = contactType match {
            case ContactType.First =>
              List(contactInfo)
            case ContactType.Second =>
              service.getContactInfo(request.userAnswers, ContactType.First).toList :+ contactInfo
          }
          for {
            updatedUserAnswers <-
              Future.fromTry(request.userAnswers.set(ContactsPage, contacts))
            _ <- sessionRepository.set(updatedUserAnswers)
          } yield {
            Redirect(navigator.nextPage(ContactCheckYourAnswersPage(contactType), NormalMode, updatedUserAnswers))
          }
        case None => Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }
    }
}
