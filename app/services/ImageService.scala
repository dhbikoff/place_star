package services

import java.io.ByteArrayInputStream

import com.sksamuel.scrimage.{Image => ScrImage}
import org.apache.commons.codec.binary.Base64
import types.Image

object ImageService {
  def resize(bytes: Array[Byte], image: Image) = {
    val input = new ByteArrayInputStream(bytes)

    val imageBytes = ScrImage.fromStream(input)
      .scaleTo(image.width, image.height)
      .forWriter(image.writer).bytes

    Base64.encodeBase64(imageBytes)
  }
}
