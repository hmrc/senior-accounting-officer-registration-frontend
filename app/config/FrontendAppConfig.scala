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

package config

import com.google.inject.{Inject, Singleton}
import models.config.FeatureToggle.*
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.{Call, RequestHeader}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import FrontendAppConfig.*

@Singleton
class FrontendAppConfig @Inject() (servicesConfig: ServicesConfig, val configuration: Configuration)
    extends FeatureConfigSupport {
  given Configuration = configuration

  val host: String    = configuration.get[String]("host").removeTrailingPathSeparator
  val appName: String = configuration.get[String]("appName")

  private val contactHost = configuration.get[String]("contact-frontend.host").removeTrailingPathSeparator
  val contactFormServiceIdentifier: String = configuration.get[String]("serviceId")

  def feedbackUrl(using request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${host + request.uri}"

  def hubUrl: String = getValue("senior-accounting-officer-hub-frontend.host")

  private def getValue(key: String): String =
    sys.props
      .get(key)
      .getOrElse(
        configuration.get[String](key)
      )

  val loginUrl: String         = configuration.get[String]("urls.login").removeTrailingPathSeparator
  val loginContinueUrl: String = configuration.get[String]("urls.loginContinue").removeTrailingPathSeparator
  val signOutUrl: String       = configuration.get[String]("urls.signOut").removeTrailingPathSeparator

  private val exitSurveyBaseUrl: String =
    configuration.get[String]("feedback-frontend.host").removeTrailingPathSeparator
  val exitSurveyUrl: String = s"$exitSurveyBaseUrl/feedback/$contactFormServiceIdentifier"

  val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("features.welsh-translation")

  def languageMap: Map[String, Lang] = Map(
    "en" -> Lang("en")
    // "cy" -> Lang("cy")
  )

  val timeout: Int   = configuration.get[Int]("timeout-dialog.timeout")
  val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  val cacheTtl: Long = configuration.get[Int]("mongodb.timeToLiveInSeconds")

  val grsBaseUrl: String            = servicesConfig.baseUrl("incorporated-entity-identification-frontend")
  val grsStubsBaseUrl: String       = servicesConfig.baseUrl("incorporated-entity-identification-frontend-stubs")
  def stubGrs: Boolean              = isEnabled(StubGrs)
  def grsAllowsRelativeUrl: Boolean = isEnabled(GrsAllowRelativeUrl)

}

object FrontendAppConfig {
  private val pathSeparator: String = "/"

  extension (str: String) {
    def removeTrailingPathSeparator: String = str.replaceAll(pathSeparator + "$", "")
  }

  extension (appConfig: FrontendAppConfig) {
    def prependHost(url: String): String =
      s"${appConfig.host}${if url.startsWith(pathSeparator) then "" else pathSeparator}$url"

    def prependHost(call: Call): String = prependHost(call.url)
  }

  def setValue(key: String, value: String): Unit =
    sys.props.addOne((key, value)): Unit
}
