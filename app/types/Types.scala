package types

import com.sksamuel.scrimage.nio.ImageWriter

import scala.concurrent.Future

trait SearchProvider {
  def search(search: String, image: Image): Future[Array[Byte]]
}

case class Image(width: Int, height: Int, writer: ImageWriter)
case class ImageFormat(writer: ImageWriter, extension: String)