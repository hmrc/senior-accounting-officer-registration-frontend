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
import models.grs.retrieve.Registration.{Registered, RegistrationFailed, RegistrationNotCalled}
import models.grs.retrieve.{CompanyProfile, CompanyDetails as GrsCompanyDetails}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import utils.IdentifierGenerator
import GrsMappingServiceSpec.*
import models.registration.CompanyDetails
import org.scalatest.EitherValues

class GrsMappingServiceSpec extends SpecBase with GuiceOneAppPerSuite with EitherValues {

  val SUT = app.injector.instanceOf[GrsMappingService]

  "GrsMappingService.map" - {
    "when GRS CompanyDetails.registration is Registered must convert to a Right(registration.CompanyDetails)" in {
      val result = SUT.map(
        GrsCompanyDetails(
          companyProfile = CompanyProfile(companyName = testCompanyName, companyNumber = testCompanyNumber),
          ctutr = testCtUtr,
          identifiersMatch = true,
          businessVerification = None,
          registration = Registered(registeredBusinessPartnerId = testRegisteredBusinessPartnerId)
        )
      )

      result mustBe Right(
        CompanyDetails(
          companyName = testCompanyName,
          companyNumber = testCompanyNumber,
          ctUtr = testCtUtr,
          registeredBusinessPartnerId = testRegisteredBusinessPartnerId
        )
      )
    }

    "when GRS CompanyDetails.registration is RegistrationFailed must return Left(Exception)" in {
      val result = SUT.map(
        GrsCompanyDetails(
          companyProfile = CompanyProfile(companyName = testCompanyName, companyNumber = testCompanyNumber),
          ctutr = testCtUtr,
          identifiersMatch = true,
          businessVerification = None,
          registration = RegistrationFailed(List.empty)
        )
      )

      result mustBe a[Left[_, _]]
      result.left.value mustBe an[Exception]
    }

    "when GRS CompanyDetails.registration is RegistrationNotCalled must return Left(Exception)" in {
      val result = SUT.map(
        GrsCompanyDetails(
          companyProfile = CompanyProfile(companyName = testCompanyName, companyNumber = testCompanyNumber),
          ctutr = testCtUtr,
          identifiersMatch = true,
          businessVerification = None,
          registration = RegistrationNotCalled
        )
      )

      result mustBe a[Left[_, _]]
      result.left.value mustBe an[Exception]
    }

  }

}

object GrsMappingServiceSpec {
  val testCompanyName: String                 = "Test Company Ltd"
  val testCompanyNumber: String               = IdentifierGenerator.randomCompanyNumber
  val testRegisteredBusinessPartnerId: String = IdentifierGenerator.randomSafeId
  val testCtUtr: String                       = IdentifierGenerator.randomUtr

}
