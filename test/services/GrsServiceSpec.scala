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
import GrsServiceSpec.*
import config.FeatureToggleSupport
import models.config.FeatureToggle.*
import models.grs.create.*
import models.registration.CompanyDetails
import org.scalatest.EitherValues
import play.api.mvc.Request
import play.api.test.FakeRequest

class GrsServiceSpec extends SpecBase with GuiceOneAppPerSuite with EitherValues with FeatureToggleSupport {

  val SUT = app.injector.instanceOf[GrsService]

  "GrsService.newRequest" - {
    given Request[_] = FakeRequest()
    "when GrsAllowRelativeUrl=true must return a request with absolute URLs" in {
      enable(GrsAllowRelativeUrl)
      val req = SUT.newRequest()

      req mustBe NewJourneyRequest(
        continueUrl = "http://localhost:10057/senior-accounting-officer/registration/business-match/result",
        businessVerificationCheck = false,
        deskProServiceId = "senior-accounting-officer-registration-frontend",
        signOutUrl = "http://localhost:10057/senior-accounting-officer/registration/account/sign-out-survey",
        regime = "VATC",
        accessibilityUrl =
          "http://localhost:12346/accessibility-statement/senior-accounting-officer/registration?referrerUrl=%2F",
        labels = ServiceLabels(en = "Senior Accounting Officer notification and certificate")
      )
    }

    "when GrsAllowRelativeUrl=false must return a request with relative URLs" in {
      disable(GrsAllowRelativeUrl)
      val req = SUT.newRequest()

      req mustBe NewJourneyRequest(
        continueUrl = "/senior-accounting-officer/registration/business-match/result",
        businessVerificationCheck = false,
        deskProServiceId = "senior-accounting-officer-registration-frontend",
        signOutUrl = "/senior-accounting-officer/registration/account/sign-out-survey",
        regime = "VATC",
        accessibilityUrl = "/accessibility-statement/senior-accounting-officer/registration?referrerUrl=%2F",
        labels = ServiceLabels(en = "Senior Accounting Officer notification and certificate")
      )
    }
  }

  "GrsService.toRelativeUrl" - {
    "must turn an absolute URL with http:// to a relative URL" in {
      val result = SUT.toRelativeUrl("http://localhost/test/2")

      result mustBe "/test/2"
    }
    "must turn an absolute URL with https:// to a relative URL" in {
      val result = SUT.toRelativeUrl("https://localhost/test/2")

      result mustBe "/test/2"
    }
    "must turn an absolute URL with port numbers to a relative URL" in {
      val result = SUT.toRelativeUrl("http://localhost:10057/test/2")

      result mustBe "/test/2"
    }
    "must turn an absolute URL with query parameters to a relative URL" in {
      val result = SUT.toRelativeUrl("http://localhost:10057/test/2?query=answer")

      result mustBe "/test/2?query=answer"
    }
    "must leave a relative URL unchanged" in {
      val result = SUT.toRelativeUrl("/test/2")

      result mustBe "/test/2"
    }
    "must leave a relative URL with query parameters unchanged" in {
      val result = SUT.toRelativeUrl("/test/2?query=answer")

      result mustBe "/test/2?query=answer"
    }
  }

  "GrsService.map" - {
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

object GrsServiceSpec {
  val testCompanyName: String                 = "Test Company Ltd"
  val testCompanyNumber: String               = IdentifierGenerator.randomCompanyNumber
  val testRegisteredBusinessPartnerId: String = IdentifierGenerator.randomSafeId
  val testCtUtr: String                       = IdentifierGenerator.randomUtr
}
