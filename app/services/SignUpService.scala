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

import connectors.EtmpSubscriptionConnector
import models.signup.{EtmpSubscriptionRequest, SignUpRequest, SignUpResponse}
import play.api.mvc.RequestHeader

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject

class SignUpService @Inject() (etmpSubscriptionConnector: EtmpSubscriptionConnector)(using ExecutionContext) {

  def signUp(request: SignUpRequest)(using RequestHeader): Future[Either[Exception, SignUpResponse]] =
    etmpSubscriptionConnector
      .subscribe(EtmpSubscriptionRequest.fromSignUpRequest(request))
      .map(_.map(response => SignUpResponse(response.saoSubscriptionId)))
}
