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
import fr.yaro.link.GetFullUrlResponse

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
              links.map(l => l.copy(shortUrl = addServerHostPrefix(l.shortUrl)))
            )
          )
        } yield resp
      case GET -> Root / id =>
        storage.expandLinkWithCount(id).flatMap {
          case Some(link) =>
            for {
              response <- Ok(GetFullUrlResponse(link))
            } yield response.withHeaders(Headers("Location" -> link.url))
          case None =>
            IO.pure(Response(NotFound))
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
        IO(e.printStackTrace()) *> IO.raiseError(e)
      }
      .build
  }
}
