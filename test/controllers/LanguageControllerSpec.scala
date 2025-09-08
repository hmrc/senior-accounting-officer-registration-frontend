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

import base.SpecBase
import config.FrontendAppConfig
import models.UserAnswers
import play.api.i18n.Lang
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsEmpty, Cookies}
import play.api.test.Helpers.*
import play.api.test.{FakeRequest, Helpers}
import play.api.{Configuration, inject}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject

class LanguageControllerSpec extends SpecBase {

  val testReferrerUrl                                                 = "test/referrer/url"
  def buildRequest(lang: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(
      GET,
      routes.LanguageSwitchController.switchToLanguage(lang).url
    ) withHeaders ("referer" -> testReferrerUrl)

  override def applicationBuilder(userAnswers: Option[UserAnswers] = None): GuiceApplicationBuilder =
    super
      .applicationBuilder(userAnswers)
      .overrides(
        bind[FrontendAppConfig].to[TestFrontendAppConfig]
      )
      .configure("play.i18n.langs" -> List("en", "cy"))

  "Language selection endpoint" - {
    "must set the language to English in " +
      "the session-cookie and redirect the user based on the referrer header, when English language is selected" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = buildRequest("en")
          val result  = route(application, request).value
          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe testReferrerUrl
          val resultCookies: Cookies = cookies(result)
          val playLang               = resultCookies.get("PLAY_LANG")
          playLang.map(_.value).value mustBe "en"
        }
      }
  }

  "must set the language to Welsh in " +
    "the session-cookie and redirect the user based on the referrer header, when Welsh language is selected" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = buildRequest("cy")
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe testReferrerUrl

        val resultCookies: Cookies = cookies(result)
        val playLang               = resultCookies.get("PLAY_LANG")
        playLang.map(_.value).value mustBe "cy"
      }
    }
}

class TestFrontendAppConfig @Inject() (servicesConfig: ServicesConfig, configuration: Configuration)
    extends FrontendAppConfig(servicesConfig, configuration) {
  override def languageMap: Map[String, Lang] = Map("en" -> Lang("en"), "cy" -> Lang("cy"))
}
