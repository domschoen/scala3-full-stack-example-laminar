package example

import io.circe.generic.semiauto._
import io.circe.Codec

case class Note(id: String, title: String, content: String) {
  def description() = title + " " + content
}

object Note {
  given Codec[Note] = deriveCodec[Note]
}