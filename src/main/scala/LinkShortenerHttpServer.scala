package fr.yaro.link

import cats.effect.{ExitCode, IO}
import com.comcast.ip4s.{ipv4, port}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder, Headers, HttpRoutes, Response}
import org.http4s.dsl.io.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router

import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*

class LinkShortenerHttpServer(storage: LinkStorage) {
  val publicHost = "http://localhost:8080"

  val helloWorldService = HttpRoutes
    .of[IO] {
      case req @ PUT -> Root / "create" =>
        for {
          request <- req.as[CreateLinkRequest]
          id <- storage.createLink(request.url)
          response = CreateLinkResponse(addServerHostPrefix(id))
          resp <- Created(response)
        } yield resp
      case GET -> Root / "list" =>
        for {
          links <- storage.listLinks()
          resp <- Ok(
            ListLinksResponse(
              links.map(l => Link(l.url, addServerHostPrefix(l.shortUrl)))
            )
          )
        } yield resp
      case GET -> Root / id =>
        storage.expandLink(id).map {
          case Some(url) =>
            Response(
              PermanentRedirect,
              headers = Headers("Location" -> url)
            )
          case None =>
            Response(NotFound)
        }
    }

  private def addServerHostPrefix(id: String): String = {
    s"$publicHost/$id"
  }

  def build() = {
    val httpApp = Router("/" -> helloWorldService).orNotFound
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(httpApp)
      .withErrorHandler { case e =>
        IO(println(s"Error handling request: $e")) *> IO.raiseError(e)
      }
      .build
  }
}
