package controllers

import com.google.inject.Inject
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller, Result}
import play.mvc.Http
import services.{BingSearch, GoogleSearch}
import utils.SearchError

import scala.concurrent.ExecutionContext.Implicits.global

class Application @Inject()(ws: WSClient, googleSearch: GoogleSearch, bingSearch: BingSearch) extends Controller {

  def ping = Action {
    Ok("pong")
  }

  def bing(search: String, width: Int, height: Int) = Action.async {
      bingSearch.get(search, width, height)
      .map(imageBytes => Ok(embedImage(imageBytes)).as(HTML))
      .recover(recoverError)
  }

  def google(search: String, width: Int, height: Int) = Action.async {
      googleSearch.get(search, width, height)
      .map(imageBytes => Ok(embedImage(imageBytes)).as(HTML))
      .recover(recoverError)
  }

  def embedImage(encodedImage: Array[Byte]) = {
    s"""<img src="data:image/jpg;base64, ${new String(encodedImage)}"></img>"""
  }

  def recoverError: PartialFunction[Throwable, Result] = {
    case err: SearchError => Status(err.status)(err.msg)
    case err: Throwable => Status(Http.Status.INTERNAL_SERVER_ERROR)(err.getMessage)
  }

}
