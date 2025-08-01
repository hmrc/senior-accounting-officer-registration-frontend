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

package models.grs.retrieve

import play.api.libs.json.{JsObject, Json, Reads}

final case class CompanyDetails(
    companyProfile: CompanyProfile,
    ctutr: String,
    identifiersMatch: Boolean,
    businessVerification: Option[BusinessVerificationStatus],
    registration: Registration
)

object CompanyDetails {
  given Reads[CompanyDetails] = Json.reads[CompanyDetails]
}

final case class CompanyProfile(
    companyName: String,
    companyNumber: String
)

object CompanyProfile {
  given Reads[CompanyProfile] = Json.reads[CompanyProfile]
}
