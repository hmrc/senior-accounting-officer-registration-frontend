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

package controllers.testonly

import config.FeatureToggleSupport
import models.config.FeatureToggle
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.testonly.FeatureToggleView

import javax.inject.Inject

class FeatureToggleController @Inject() (
    override val messagesApi: MessagesApi,
    featureToggleView: FeatureToggleView,
    val controllerComponents: MessagesControllerComponents
) extends FrontendBaseController
    with I18nSupport
    with FeatureToggleSupport {

  def get: Action[AnyContent] = Action { implicit request =>
    Ok(featureToggleView())
  }

  def update: Action[Map[String, Seq[String]]] = Action(parse.formUrlEncoded) { implicit request =>
    val enableList =
      request.body.toSeq.filter((k, _) => k != "csrfToken").map((_, v) => FeatureToggle.fromOrdinal(v.head.toInt))
    FeatureToggle.values.foreach(feature => {
      if (enableList.contains(feature)) {
        enable(feature)
      } else {
        disable(feature)
      }
    })
    Ok(featureToggleView())
  }
}
