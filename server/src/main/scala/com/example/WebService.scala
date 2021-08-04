package com.example

import akka.http.scaladsl.server.Directives
import com.example.twirl.Implicits._

class WebService() extends Directives {

  val route = {
    pathSingleSlash {
      get {
        complete {
          com.example.html.index.render()
        }
      }
    } ~
      pathPrefix("assets" / Remaining) { file =>
        // optionally compresses the response with Gzip or Deflate
        // if the client accepts compressed responses
        encodeResponse {
          getFromResource("public/" + file)
        }
      }
  }
}
