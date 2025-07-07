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
import forms.{AddAnotherFormProvider, PrimaryContactDetailsFormProvider}
import models.{Mode, NormalMode}
import navigation.Navigator
import pages.PrimaryContactDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.PrimaryContactDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PrimaryContactDetailsController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: Navigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: PrimaryContactDetailsFormProvider,
    addAnotherFormProvider: AddAnotherFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: PrimaryContactDetailsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form           = formProvider()
  val addAnotherForm = addAnotherFormProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>

    val preparedForm = request.userAnswers.get(PrimaryContactDetailsPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val addAnother = addAnotherForm.bindFromRequest().get
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(PrimaryContactDetailsPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield
              if (addAnother) {
                Redirect(routes.SecondaryContactDetailsController.onPageLoad(NormalMode))
              } else {
                Redirect(routes.DashboardController.onPageLoad())
              }
        )
    }

}
