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
import forms.ContactHaveYouAddedAllFormProvider
import models.{ContactType, NormalMode}
import navigation.Navigator
import pages.ContactHaveYouAddedAllPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ContactHaveYouAddedAllView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ContactHaveYouAddedAllController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: Navigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: ContactHaveYouAddedAllFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: ContactHaveYouAddedAllView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(contactType: ContactType): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>

      val preparedForm = request.userAnswers.get(ContactHaveYouAddedAllPage(contactType)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, contactType))
    }

  def onSubmit(contactType: ContactType): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, contactType))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(ContactHaveYouAddedAllPage(contactType), value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(ContactHaveYouAddedAllPage(contactType), NormalMode, updatedAnswers))
        )
    }
}
