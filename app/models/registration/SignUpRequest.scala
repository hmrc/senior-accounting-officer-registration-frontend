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

package models.registration

import play.api.libs.json.{Json, OFormat}

final case class NominatedCompany(name: String, utr: String, crn: String)

object NominatedCompany {
  given OFormat[NominatedCompany] = Json.format
}

final case class Contact(name: String, email: String, status: String, language: String)

object Contact {
  given OFormat[Contact] = Json.format
}

final case class SignUpRequest(
    etmpSafeId: String,
    nominatedCompany: NominatedCompany,
    contacts: List[Contact]
)

object SignUpRequest {
  given OFormat[SignUpRequest] = Json.format[SignUpRequest]
}
