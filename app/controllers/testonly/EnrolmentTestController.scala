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

package controllers.testonly

import config.AppConfig
import controllers.routes
import play.api.Logger
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import play.api.mvc.MessagesControllerComponents
import play.api.mvc.Results.Redirect
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.{Credentials, ~}
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisationException, AuthorisedFunctions, NoActiveSession}
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps, UnauthorizedException}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class Enrolment(serviceName: String, identifier: String, value: String)

// In order to switch to our enrolment we can set tax-enrolment configs
// sm2 --start --appendArgs '{"TAX_ENROLMENTS":["-Dservices-to-activate=HMRC-SAO-ORG"]}' TAX_ENROLMENTS ENROLMENT_STORE_STUB
// but we must also (get them to) update ENROLMENT_STORE_STUB since it's hard coded there
val testEnrolmentTarget = Enrolment("HMRC-SAO-ORG", "saoRefNumber", "1234567890")

//val testEnrolmentTarget = Enrolment("IR-SA", "UTR", "1234567890")

class EnrolmentTestController @Inject() (
    override val authConnector: AuthConnector,
    val controllerComponents: MessagesControllerComponents,
    config: AppConfig,
    httpClient: HttpClientV2
)(using ExecutionContext)
    extends FrontendBaseController
    with AuthorisedFunctions {

  def enrol = Action.async { implicit request =>
    Logger(getClass).error(hc.authorization.map(_.value).getOrElse(""))

    authorised()
      .retrieve(
        Retrievals.internalId and Retrievals.affinityGroup and Retrievals.credentials and Retrievals.groupIdentifier and Retrievals.allEnrolments
      ) {
        case internalId ~ Some(affinityGroup) ~ Some(Credentials(providerId, _)) ~ Some(groupIdentifier) ~ enrolments =>
          Logger(getClass).error("internalId=" + internalId)
          Logger(getClass).error("affinityGroup=" + affinityGroup)
          Logger(getClass).error("providerId=" + providerId)
          Logger(getClass).error("groupIdentifier=" + groupIdentifier)
          Logger(getClass).error("enrolments=" + enrolments.enrolments.mkString(","))

          internalId
            .map { internalId =>
              for {
                r <- stubEnrolment(
                  // since enrolment-store-stub doesn't support our enrolment just yet
                  enrolment = testEnrolmentTarget.copy(serviceName = "IR-SA", identifier = "UTR"),
                  groupId = groupIdentifier,
                  affinityGroup = affinityGroup.toString,
                  credId = providerId
                )
                r <- callTaxEnrolment(enrolment = testEnrolmentTarget)
              } yield Ok(s"status=${r.status} body=${r.body}")

            }
            .getOrElse(throw new UnauthorizedException("Unable to retrieve internal Id"))
      }
      .recover {
        case _: NoActiveSession =>
          Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
        case _: AuthorisationException =>
          Redirect(routes.UnauthorisedController.onPageLoad())
      }

  }

  private def callTaxEnrolment(enrolment: Enrolment)(using HeaderCarrier): Future[HttpResponse] = {
    val url = s"http://localhost:9995/tax-enrolments/service/${enrolment.serviceName}/enrolment"
    httpClient
      .put(url"${url}")
      // locally there are no checks for the known facts, so we can set them to desired values
      .withBody(Json.parse(s"""{
           |  "identifiers": [ { "key": "${enrolment.identifier}", "value": "${enrolment.value}" } ],
           |  "verifiers": [
           |          {
           |              "key": "UTR",
           |              "value": "1234567890"
           |          },
           |          {
           |              "key": "CRN",
           |              "value": "12345678"
           |          }
           |   ]
           |}
           |""".stripMargin))
      .execute[HttpResponse]
  }

  private def stubEnrolment(enrolment: Enrolment, groupId: String, affinityGroup: String, credId: String)(using
      HeaderCarrier
  ): Future[HttpResponse] = {
    // until we can update enrolment-store-stub we cannot stub it to return our desired enrolment
    // take note of `groupId`, `users.credId` & the `assignedUserCreds` linking back to the user
    // also take note, once a stub has been created for given user/group combination, it cannot be updated using this API
    httpClient
      .post(url"http://localhost:9595/enrolment-store-stub/data")
      .withBody(
        Json.parse(
          s"""{
         |        "groupId": "$groupId",
         |        "affinityGroup": "$affinityGroup",
         |        "users": [
         |                {
         |                        "credId": "$credId",
         |                        "name": "Default User",
         |                        "email": "default@example.com",
         |                        "credentialRole": "Admin",
         |                        "description": "User Description"
         |                }
         |        ],
         |        "enrolments": [
         |		{
         |			"serviceName": "${enrolment.serviceName}",
         |			"identifiers": [
         |				{
         |					"key": "${enrolment.identifier}",
         |					"value": "${enrolment.value}"
         |				}
         |			],
         |			"assignedUserCreds": [
         |				"$credId"
         |			],
         |			"state": "Activated",
         |			"enrolmentType": "principal",
         |			"assignedToAll": false
         |		}
         |        ]
         |}
         |
         |""".stripMargin
        )
      )
      .execute[HttpResponse]
  }

}
