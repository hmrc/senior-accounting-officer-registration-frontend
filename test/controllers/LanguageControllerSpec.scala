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
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Application
import play.api.i18n.Lang
import play.api.mvc.{Cookie, Cookies, MessagesControllerComponents}
import play.api.test.Helpers.*
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.play.language.LanguageUtils

class LanguageControllerSpec extends SpecBase {

  val app: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
    .configure("play.i18n.langs" -> List("en", "cy"))
    .build()

  given cc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  val langUtils: LanguageUtils = app.injector.instanceOf[LanguageUtils]

  val mockAppConfig: FrontendAppConfig   = mock[FrontendAppConfig]
  val mockLanguageMap: Map[String, Lang] = Map("en" -> Lang("en"), "cy" -> Lang("cy"))
  when(mockAppConfig.languageMap).thenReturn(mockLanguageMap)

  object TestLanguageSwitchController extends LanguageSwitchController(mockAppConfig, langUtils, cc)

  def testLanguageSelection(language: String, expectedCookieValue: String): Unit = {
    val request                = FakeRequest()
    val result                 = TestLanguageSwitchController.switchToLanguage(language)(request)
    val resultCookies: Cookies = cookies(result)
    resultCookies.size mustBe 1
    val cookie: Cookie = resultCookies.head
    cookie.name mustBe "PLAY_LANG"
    cookie.value mustBe expectedCookieValue
  }

  "Hitting language selection endpoint" - {

    "redirect to English translated start page if English language is selected" in {
      testLanguageSelection("en", "en")
    }

    "redirect to Welsh translated start page if Welsh language is selected" in {
      testLanguageSelection("cy", "cy")
    }

  }

}
