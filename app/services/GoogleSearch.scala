package services

import com.google.inject.Inject
import play.Logger
import play.api.Play
import play.api.Play.current
import play.api.libs.ws.{WSClient, WSResponse}
import types.{Image, SearchProvider}
import utils.SearchErrorHandler

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

class GoogleSearch @Inject()(ws: WSClient) extends SearchProvider {
  val googleUrl = Play.configuration.getString("google.search.url").get
  val googleApiKey = Play.configuration.getString("google.api.key").get
  val googleSearchKey = Play.configuration.getString("google.search.key").get

  override def search(search: String, image: Image) = {
    searchGoogle(search).map(r => processGoogleResponse(r).get)
      .flatMap(imageUrl => fetchGoogleImage(imageUrl.toString()))
      .map(imageResult => ImageService.resize(imageResult.bodyAsBytes, image))
  }

  private def searchGoogle(search: String) = {
    ws.url(googleUrl)
      .withQueryString("q" -> search)
      .withQueryString("cx" -> googleSearchKey)
      .withQueryString("key" -> googleApiKey)
      .withQueryString("imgSize" -> "large")
      .withQueryString("safe" -> "medium")
      .get()
  }

  private def processGoogleResponse(response: WSResponse) = {
    SearchErrorHandler.err("Google", response)

    val items = response.json \ "items"
    val pagemaps = items \\ "pagemap"
    Logger.info(s"Found ${pagemaps.length} results from Google")

    val index = Random.nextInt(pagemaps.length)
    val cseImg = (pagemaps(index) \ "cse_image").head
    cseImg \ "src"
  }

  private def fetchGoogleImage(url: String) = {
    ws.url(url.replace("\"", "")).get()
  }
}
