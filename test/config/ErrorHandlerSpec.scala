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

package config

import base.SpecBase
import com.mongodb.MongoException
import handlers.ErrorHandler
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class ErrorHandlerSpec extends SpecBase with GuiceOneAppPerSuite {

  given RequestHeader = FakeRequest("GET", "/")

  private val handler = app.injector.instanceOf[ErrorHandler]

  "standardErrorTemplate must" - {
    "render HTML" in {
      val html = handler.standardErrorTemplate("title", "heading", "message").futureValue
      html.contentType mustBe "text/html"
    }
  }
  "NotFoundError must" - {
    "render HTML" in {
      val html = handler.notFoundTemplate.futureValue
      html.contentType mustBe "text/html"
    }
  }
  "internalServerErrorTemplate must" - {
    "render HTML" in {
      val html = handler.internalServerErrorTemplate.futureValue
      html.contentType mustBe "text/html"
    }
  }

  "onServerError must" - {

    "redirect to JourneyRecoveryController when a MongoException occurs" in {
      val request = FakeRequest("GET", "/")
      val result  = handler.onServerError(request, new MongoException("boom")).futureValue

      result.header.status mustBe SEE_OTHER
      result.header.headers.get(LOCATION) mustBe Some(
        controllers.routes.JourneyRecoveryController.onPageLoad().url
      )
    }

    "redirect to JourneyRecoveryController when a MongoException is wrapped inside another exception's cause" in {
      val request      = FakeRequest("GET", "/")
      val wrappedMongo = new RuntimeException("wrapper", new MongoException("boom"))
      val result       = handler.onServerError(request, wrappedMongo).futureValue

      result.header.status mustBe SEE_OTHER
      result.header.headers.get(LOCATION) mustBe Some(
        controllers.routes.JourneyRecoveryController.onPageLoad().url
      )
    }

    "fall back to the default error handling for a non-Mongo exception" in {
      val request = FakeRequest("GET", "/")
      val result  = handler.onServerError(request, new RuntimeException("not mongo related")).futureValue

      result.header.status mustBe INTERNAL_SERVER_ERROR
    }
  }

}
