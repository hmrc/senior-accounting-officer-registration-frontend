package controllers.actions

import scala.concurrent.Future
import models.requests.OptionalDataRequest
import javax.inject.Inject
import repositories.SessionRepository
import scala.concurrent.ExecutionContext

class RedirectActionImpl @Inject() (
    val sessionRepository: SessionRepository
)(implicit val executionContext: ExecutionContext){
    override protected def transform[A](): Future[OptionalDataRequest[A]] = {

    sessionRepository.get(request.userId).map {
      OptionalDataRequest(request.request, request.userId, _)
    }
  }
}
