/*
 * Copyright 2026 HM Revenue & Customs
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

package forms.beta

import config.AppConfig
import forms.behaviours.StringFieldBehaviours
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.data.FormError

class BetaLoginFormProviderSpec extends StringFieldBehaviours {

  val misMatchKey              = "betaLogin.error.passwordMismatch"
  val lengthKey                = "betaLogin.error.length"
  val maxLength                = 100
  val mockAppConfig: AppConfig = mock[AppConfig]
  val testPassword             = "testPassword"
  when(mockAppConfig.privateBetaPassword).thenReturn(testPassword)

  val form = new BetaLoginFormProvider(mockAppConfig)()

  ".value" - {

    val fieldName = "value"

    "must bind for correct password" in {
      val result = form.bind(Map(fieldName -> testPassword)).apply(fieldName)
      result.value.value mustBe testPassword
      result.errors mustBe empty
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, misMatchKey)
    )

    "must not bind invalid data" in {

      forAll(stringsExceptSpecificValues(Seq(testPassword)) -> "invalidPassword") { (dataItem: String) =>
        val result = form.bind(Map(fieldName -> dataItem)).apply(fieldName)

        result.errors mustEqual Seq(FormError(fieldName, misMatchKey))
      }
    }
  }

  "error message keys must map to the expected text" - {
    createTestWithErrorMessageAssertion(
      key = misMatchKey,
      message = "The password is not correct"
    )
  }
}
