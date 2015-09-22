package services

import java.io.ByteArrayInputStream

import com.sksamuel.scrimage.Image
import org.apache.commons.codec.binary.Base64

object ImageService {
  def resize(bytes: Array[Byte], width: Int, height: Int) = {
    val input = new ByteArrayInputStream(bytes)
    val imageBytes = Image.fromStream(input).scaleTo(width, height).bytes
    Base64.encodeBase64(imageBytes)
  }
}
