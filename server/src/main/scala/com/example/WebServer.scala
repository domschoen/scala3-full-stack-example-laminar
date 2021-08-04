package example

import akka.actor.ActorSystem
import akka.http.scaladsl.*
import com.typesafe.config.ConfigFactory

import java.nio.file.Paths

import scala.concurrent.ExecutionContext
import com.example.twirl.Implicits._
import akka.http.scaladsl.model.headers.`Cache-Control`
import akka.http.scaladsl.model.headers.CacheDirectives.`no-cache`
import akka.http.scaladsl.server.directives.CachingDirectives.cachingProhibited

object WebServer extends server.Directives with CirceSupport:
  @main def start =
    given system: ActorSystem = ActorSystem("webserver")
    given ExecutionContext = system.dispatcher

    val config = ConfigFactory.load()
    val interface = config.getString("http.interface")
    val port = config.getInt("http.port")
    val directory = Paths.get(config.getString("example.directory"))

    val repository = Repository(directory)
    Http()
      .newServerAt(interface, port)
      .bindFlow(base ~ assets ~ api(repository))
    println(s"Server online at http://$interface:$port/")


  private val base: server.Route =
      pathSingleSlash(
        get {
            complete {
              com.example.html.index.render()
            }
        }
      )


  private val assets: server.Route =
    pathPrefix("assets" / Remaining) { file =>
      // optionally compresses the response with Gzip or Deflate
      // if the client accepts compressed responses
      encodeResponse {
        getFromResource("public/" + file)
      }
    }


  private def api(repository: Repository): server.Route =
    path("api" / "notes")(
      get (
        complete(repository.getAllNotes())
      ) ~
        post (
          entity(as[CreateNote]) { request =>
            complete(repository.createNote(request.title, request.content))
          }
        )
    )
