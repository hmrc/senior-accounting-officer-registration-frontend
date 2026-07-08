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

package controllers.testonly

import config.AppConfig
import controllers.actions.{ApiAuthenticatedIdentifierAction, IdentifierAction}
import controllers.testonly.GrsStubsController.*
import forms.mappings.Mappings
import models.grs.create.NewJourneyResponse
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.*
import play.api.data.*
import play.api.data.Forms.mapping
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{Format, JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.mdc.Mdc
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.IdentifierGenerator
import views.html.testonly.StubGrsView

import scala.concurrent.{ExecutionContext, Future}

import java.time.{Clock, Instant}
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}

class GrsStubsController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    apiIdentify: ApiAuthenticatedIdentifierAction,
    stubGrsView: StubGrsView,
    val controllerComponents: MessagesControllerComponents,
    grsStubsRepo: GrsStubRepository,
    appConfig: AppConfig
)(using ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def startGrs(): Action[AnyContent] = apiIdentify { implicit request =>
    val uuid            = UUID.randomUUID()
    val journeyStartUrl = appConfig.prependHost(routes.GrsStubsController.getStubGrs(uuid.toString))

    Created(Json.toJson(NewJourneyResponse(journeyStartUrl)))
  }

  def getStubGrs(journeyId: String): Action[AnyContent] = identify { implicit request =>
    Ok(stubGrsView(GrsResponseConfig.form, journeyId))
  }

  def postStubGrs(journeyId: String): Action[AnyContent] = identify.async { implicit request =>
    val redirectUrl = appConfig.prependHost(controllers.routes.GrsController.callBack(journeyId))
    GrsResponseConfig.form
      .bindFromRequest()
      .fold(
        _ => Future.successful(InternalServerError("Form error")),
        config =>
          grsStubsRepo
            .set(
              GrsConfig(
                journeyId,
                config
              )
            )
            .map(_ => SeeOther(redirectUrl))
      )
  }

  def getGrs(journeyId: String): Action[AnyContent] = apiIdentify.async { implicit request =>
    grsStubsRepo
      .get(journeyId)
      .map { case GrsConfig(_, GrsResponseConfig(status, body), _) =>
        Status(status)(body).withHeaders(
          CONTENT_TYPE -> "application/json"
        )
      }
  }

}

object GrsStubsController {
  private val default200Json: JsObject =
    Json.obj(
      "companyProfile" -> Json.obj(
        "companyName"            -> "Test Company Ltd",
        "companyNumber"          -> IdentifierGenerator.randomCompanyNumber,
        "dateOfIncorporation"    -> "2020-01-01",
        "unsanitisedCHROAddress" -> Json.obj(
          "address_line_1" -> "testLine1",
          "address_line_2" -> "test town",
          "care_of"        -> "test name",
          "country"        -> "United Kingdom",
          "locality"       -> "test city",
          "po_box"         -> "123",
          "postal_code"    -> "AA11AA",
          "premises"       -> "1",
          "region"         -> "test region"
        )
      ),
      "identifiersMatch" -> true,
      "registration"     -> Json.obj(
        "registrationStatus"          -> "REGISTERED",
        "registeredBusinessPartnerId" -> IdentifierGenerator.randomSafeId
      ),
      "ctutr" -> IdentifierGenerator.randomUtr
    )

  final case class GrsResponseConfig(status: Int, body: String)

  object GrsResponseConfig extends Mappings {
    def default200: GrsResponseConfig       = GrsResponseConfig(status = 200, body = Json.stringify(default200Json))
    given format: Format[GrsResponseConfig] = Json.format[GrsResponseConfig]

    def form: Form[GrsResponseConfig] = Form(
      mapping(
        "status" -> int(),
        "body"   -> text()
      )(GrsResponseConfig.apply)(o => Some(Tuple.fromProductTyped(o)))
    ).fill(default200)
  }

  final case class GrsConfig(journeyId: String, response: GrsResponseConfig, lastUpdated: Instant = Instant.now)

  private object GrsConfig {
    def default200(journeyId: String): GrsConfig = GrsConfig(
      journeyId = journeyId,
      response = GrsResponseConfig.default200
    )

    given instantFormat: Format[Instant] = MongoJavatimeFormats.instantFormat
    given format: Format[GrsConfig]      = Json.format[GrsConfig]
  }

  @Singleton
  class GrsStubRepository @Inject() (
      mongoComponent: MongoComponent,
      appConfig: AppConfig,
      clock: Clock
  )(using ec: ExecutionContext)
      extends PlayMongoRepository[GrsConfig](
        collectionName = "grs-stub",
        mongoComponent = mongoComponent,
        domainFormat = GrsConfig.format,
        indexes = Seq(
          IndexModel(
            Indexes.ascending("journeyId"),
            IndexOptions().name("journeyIdIdx")
          ),
          IndexModel(
            Indexes.ascending("lastUpdated"),
            IndexOptions()
              .name("lastUpdatedIdx")
              .expireAfter(appConfig.cacheTtl, TimeUnit.SECONDS)
          )
        )
      ) {

    given instantFormat: Format[Instant] = MongoJavatimeFormats.instantFormat

    private def byJourneyId(journeyId: String): Bson = Filters.equal("journeyId", journeyId)

    def keepAlive(journeyId: String): Future[Boolean] = Mdc.preservingMdc {
      collection
        .updateOne(
          filter = byJourneyId(journeyId),
          update = Updates.set("lastUpdated", Instant.now(clock))
        )
        .toFuture()
        .map(_ => true)
    }

    def get(journeyId: String): Future[GrsConfig] = Mdc.preservingMdc {
      keepAlive(journeyId)
        .flatMap { _ =>
          collection
            .find(byJourneyId(journeyId))
            .headOption()
        }
        .map(_.fold(GrsConfig.default200(journeyId))(identity))
    }

    def set(config: GrsConfig): Future[Boolean] = Mdc.preservingMdc {

      val updatedConfig = config copy (lastUpdated = Instant.now(clock))

      collection
        .replaceOne(
          filter = byJourneyId(updatedConfig.journeyId),
          replacement = updatedConfig,
          options = ReplaceOptions().upsert(true)
        )
        .toFuture()
        .map(_ => true)
    }

    def clear(journeyId: String): Future[Boolean] = Mdc.preservingMdc {
      collection
        .deleteOne(byJourneyId(journeyId))
        .toFuture()
        .map(_ => true)
    }
  }

}
