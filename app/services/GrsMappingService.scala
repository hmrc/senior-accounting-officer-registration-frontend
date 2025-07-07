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

import models.registration.CompanyDetails
import models.grs.retrieve.{Registration, CompanyDetails as GrsCompanyDetails}
import uk.gov.hmrc.http.InternalServerException

class GrsMappingService {
  def map(companyDetails: GrsCompanyDetails): Either[Exception, CompanyDetails] =
    companyDetails.registration match {
      case Registration.Registered(id) =>
        Right(
          CompanyDetails(
            companyName = companyDetails.companyProfile.companyName,
            companyNumber = companyDetails.companyProfile.companyNumber,
            ctUtr = companyDetails.ctutr,
            registeredBusinessPartnerId = id
          )
        )
      case _ => Left(new InternalServerException("Unable to convert company details"))
    }

}
