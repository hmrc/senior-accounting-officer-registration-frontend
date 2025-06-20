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

import com.google.inject.AbstractModule
import registration.controllers.{actions => registrationActions}
import eligibility.controllers.{actions => eligibilityActions}

import java.time.{Clock, ZoneOffset}

class Module extends AbstractModule {

  override def configure(): Unit = {

    bind(classOf[registrationActions.DataRetrievalAction])
      .to(classOf[registrationActions.DataRetrievalActionImpl])
      .asEagerSingleton()
    bind(classOf[registrationActions.DataRequiredAction])
      .to(classOf[registrationActions.DataRequiredActionImpl])
      .asEagerSingleton()

    // For session based storage instead of cred based, change to SessionIdentifierAction
    bind(classOf[registrationActions.IdentifierAction])
      .to(classOf[registrationActions.AuthenticatedIdentifierAction])
      .asEagerSingleton()

    bind(classOf[eligibilityActions.DataRetrievalAction])
      .to(classOf[eligibilityActions.DataRetrievalActionImpl])
      .asEagerSingleton()
    bind(classOf[eligibilityActions.DataRequiredAction])
      .to(classOf[eligibilityActions.DataRequiredActionImpl])
      .asEagerSingleton()

    // For session based storage instead of cred based, change to SessionIdentifierAction
    bind(classOf[eligibilityActions.IdentifierAction])
      .to(classOf[eligibilityActions.AuthenticatedIdentifierAction])
      .asEagerSingleton()

    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone.withZone(ZoneOffset.UTC))
  }
}
