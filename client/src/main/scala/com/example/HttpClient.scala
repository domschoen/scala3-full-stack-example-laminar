package example

import org.scalajs.dom.experimental._
import scala.scalajs.js

import java.io.IOException

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.concurrent._
import ExecutionContext.Implicits.global

import io.circe.scalajs._
import io.circe.syntax._
import io.circe.parser.decode
import io.circe.Printer

import cats.syntax.either._

class HttpClient extends NoteService {
  private val printer: Printer = Printer(
    dropNullValues = true,
    indent = ""
  )

  def getAllNotes(): Future[Seq[Note]] = {
    for (
      resp <- Fetch.fetch("./api/notes").toFuture;
      json <- resp.jsonOrFailure
    )
    yield decodeJs[Seq[Note]](json).valueOr(throw _)
  }

  def createNote(title: String, content: String): Future[Note] = {
    val request = Request(
      "./api/notes",
      new {
        method = HttpMethod.POST
        headers = js.Dictionary("Content-Type" -> "application/json")
        body = printer.print(CreateNote(title, content).asJson)
      }
    )
    for (
      resp <- Fetch.fetch(request).toFuture;
      json <- resp.jsonOrFailure
    )
    yield decodeJs[Note](json).valueOr(throw _)
  }

  extension (resp: Response)
    private def jsonOrFailure: Future[js.Any] =
      if (resp.ok) {
        resp.json.toFuture
      } else Future.failed(new IOException(resp.statusText))

}