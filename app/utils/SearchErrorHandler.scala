package utils

import play.api.Logger
import play.api.libs.ws.{WSAuthScheme, WSResponse}
import play.mvc.Http

case class SearchError(status: Int, msg: String) extends Throwable

object SearchErrorHandler {
  def err(provider: String, response: WSResponse) = {
    if (response.status != Http.Status.OK) {
      val msg: String = s"$provider status: $response.statusText\n$response.body"
      Logger.logger.error(msg)
      throw SearchError(response.status, msg)
    }
  }


}
