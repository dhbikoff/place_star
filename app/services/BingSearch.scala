package services

import com.google.inject.Inject
import play.api.Play.current
import play.api.libs.ws.{WSAuthScheme, WSClient, WSResponse}
import play.api.{Logger, Play}
import utils.SearchErrorHandler

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

class BingSearch @Inject()(ws: WSClient) {
  val bingUrl = Play.configuration.getString("bing.search.url").get
  val bingApiKey = Play.configuration.getString("bing.api.key").get

  def get(search: String, width: Int, height: Int) = {
    val formattedSearch = search.replace("_", " ")
    searchBing(formattedSearch).map(res => processBingResults(res))
      .flatMap(value => fetchBingImage(value.toString()))
      .map(res => ImageService.resize(res.bodyAsBytes, width, height))
  }

  private def searchBing(search: String) = {
    ws.url(bingUrl)
      .withAuth(bingApiKey, bingApiKey, WSAuthScheme.BASIC)
      .withQueryString("$format" -> "JSON")
      .withQueryString("$top" -> "20")
      .withQueryString("Query" -> s"'$search'")
      .withQueryString("ImageFilters" -> "'Size:Large'")
      .get()
  }

  private def fetchBingImage(url: String) = {
    ws.url(url.replace("\"", "")).get()
  }

  private def processBingResults(response: WSResponse) = {
    SearchErrorHandler.err("Bing", response)

    val results = response.json \ "d" \ "results"
    val urls = results \\ "MediaUrl"
    Logger.info(s"Found ${urls.length} results from Bing")

    val index = Random.nextInt(urls.length)
    urls(index)
  }
}
