package controllers

import com.google.inject.Inject
import com.sksamuel.scrimage.nio.{JpegWriter, PngWriter}
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller, Result}
import play.mvc.Http
import services.{BingSearch, GoogleSearch}
import types.{ImageFormat, Image}
import utils.SearchError

import scala.concurrent.ExecutionContext.Implicits.global

class Application @Inject()(ws: WSClient, googleSearch: GoogleSearch, bingSearch: BingSearch) extends Controller {

  def ping = Action {
    Ok("pong")
  }

  def search(provider: String, search: String, width: Int, height: Int, imgType: String) = Action.async {
    val imgInfo = imageType(imgType)
    val image = Image(width, height, imgInfo.writer)
    val searchProvider = matchSearchProvider(provider)

    searchProvider.search(search, image)
      .map(imageBytes => Ok(embedImage(imageBytes, imgInfo.extension)).as(HTML))
      .recover(recoverError)
  }

  def embedImage(encodedImage: Array[Byte], imgExtension: String) = {
    s"""<img src="data:image/$imgExtension;base64, ${new String(encodedImage)}"></img>"""
  }

  def matchSearchProvider(provider: String) = provider.toLowerCase match {
    case "google" => googleSearch
    case _ => bingSearch
  }

  def imageType(imgType: String) = imgType.toLowerCase match {
    case "png" => ImageFormat(PngWriter(), "png")
    case _ => ImageFormat(JpegWriter(), "jpeg")
  }

  def recoverError: PartialFunction[Throwable, Result] = {
    case err: SearchError => Status(err.status)(err.msg)
    case err: Throwable => Status(Http.Status.INTERNAL_SERVER_ERROR)(err.getMessage)
  }
}
