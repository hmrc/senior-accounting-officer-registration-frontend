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

package services

import base.SpecBase
import connectors.EtmpSubscriptionConnector
import models.signup.{EtmpSubscriptionResponse, SignUpRequest, SignUpResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Request
import play.api.test.FakeRequest
import uk.gov.hmrc.http.InternalServerException

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import SignUpServiceSpec.*

class SignUpServiceSpec extends SpecBase with MockitoSugar {

  given Request[?] = FakeRequest()

  "SignUpService.signUp" - {
    "must return the subscription ID from ETMP" in {
      val connector = mock[EtmpSubscriptionConnector]
      val service   = SignUpService(connector)

      when(connector.subscribe(any())(using any())).thenReturn(
        Future.successful(Right(EtmpSubscriptionResponse(testSubscriptionId)))
      )

      service.signUp(SignUpRequest("UTR", testUtr)).futureValue mustBe Right(
        SignUpResponse(testSubscriptionId)
      )

      verify(connector).subscribe(any())(using any())
    }

    "must return the ETMP error when subscription fails" in {
      val connector = mock[EtmpSubscriptionConnector]
      val service   = SignUpService(connector)
      val error     = new InternalServerException("ETMP failed")

      when(connector.subscribe(any())(using any())).thenReturn(Future.successful(Left(error)))

      service.signUp(SignUpRequest("UTR", testUtr)).futureValue mustBe Left(error)
    }
  }
}

object SignUpServiceSpec {
  val testSubscriptionId = "XE0001234567890"
  val testUtr            = "1234567890"
}
