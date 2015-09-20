package controllers

import java.io.ByteArrayInputStream

import com.google.inject.Inject
import com.sksamuel.scrimage.Image
import org.apache.commons.codec.binary.Base64
import play.api.Play.current
import play.api.libs.ws.{WSAuthScheme, WSClient, WSResponse}
import play.api.mvc._
import play.api.{Logger, Play}
import play.mvc.Http

import scala.util.Random

class Application @Inject()(ws: WSClient) extends Controller {
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext

  val LOG = Logger.logger

  val googleUrl = Play.configuration.getString("google.search.url").get
  val googleApiKey = Play.configuration.getString("google.api.key").get
  val googleSearchKey = Play.configuration.getString("google.search.key").get

  val bingUrl = Play.configuration.getString("bing.search.url").get
  val bingApiKey = Play.configuration.getString("bing.api.key").get

  def ping = Action {
    Ok("pong")
  }

  def bing(search: String, width: Int, height: Int) = Action.async {
    bingSearch(search).map(res => processBingResults(res))
      .flatMap(value => fetchBingImage(value.toString()))
      .map(res => resizeImage(res.bodyAsBytes, width, height))
      .map(imageBytes => Ok(embedImage(imageBytes)).as(HTML))
      .recover(recoverError)
  }

  def bingSearch(search: String) = {
    ws.url(bingUrl)
      .withAuth(bingApiKey, bingApiKey, WSAuthScheme.BASIC)
      .withQueryString("$format" -> "JSON")
      .withQueryString("$top" -> "20")
      .withQueryString("Query" -> s"""'$search'""")
      .withQueryString("ImageFilters" -> "'Size:Large'")
      .get()
  }

  def recoverError: PartialFunction[Throwable, Result] = {
    case err: SearchError => Status(err.status)(err.msg)
    case err: Throwable => Status(Http.Status.INTERNAL_SERVER_ERROR)(err.getMessage)
  }

  def fetchBingImage(url: String) = {
    ws.url(url.replace("\"", "")).get()
  }

  def processBingResults(response: WSResponse) = {
    searchErrorHandler("Bing", response)

    val results = response.json \ "d" \ "results"
    val urls = results \\ "MediaUrl"
    LOG.info(s"Found ${urls.length} results from Bing")
    val index = Random.nextInt(urls.length)
    urls(index)
  }

  def google(search: String, width: Int, height: Int) = Action.async {
    googleSearch(search).map(r => processGoogleResponse(r).get)
      .flatMap(imageUrl => fetchGoogleImage(imageUrl.toString()))
      .map(imageResult => resizeImage(imageResult.bodyAsBytes, width, height))
      .map(imageBytes => Ok(embedImage(imageBytes)).as(HTML))
      .recover(recoverError)
  }

  def googleSearch(search: String) = {
    ws.url(googleUrl)
      .withQueryString("q" -> search)
      .withQueryString("cx" -> googleSearchKey)
      .withQueryString("key" -> googleApiKey)
      .withQueryString("imgSize" -> "large")
      .withQueryString("safe" -> "medium")
      .get()
  }

  def embedImage(encodedImage: Array[Byte]) = {
    s"""<img src="data:image/jpg;base64, ${new String(encodedImage)}"></img>"""
  }

  def fetchGoogleImage(url: String) = {
    ws.url(url.replace("\"", "")).get()
  }

  def resizeImage(bytes: Array[Byte], width: Int, height: Int) = {
    val input = new ByteArrayInputStream(bytes)
    val imageBytes = Image.fromStream(input).scaleTo(width, height).bytes
    Base64.encodeBase64(imageBytes)
  }

  def processGoogleResponse(response: WSResponse) = {
    searchErrorHandler("Google", response)

    val items = response.json \ "items"
    val pagemaps = items \\ "pagemap"
    LOG.info(s"Found ${pagemaps.length} results from Google")
    val index = Random.nextInt(pagemaps.length)

    val cseImg = (pagemaps(index) \ "cse_image").head
    cseImg \ "src"
  }

  case class SearchError(status: Int, msg: String) extends Throwable

  def searchErrorHandler(provider: String, response: WSResponse) = {
    if (response.status != Http.Status.OK) {
      val msg: String = s"$provider status: $response.statusText\n$response.body"
      LOG.error(msg)
      throw SearchError(response.status, msg)
    }
  }
}
