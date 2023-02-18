package fr.yaro.link

import cats.effect.*
import com.comcast.ip4s.{ ipv4, port }
import org.http4s.dsl.io.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.{ EntityDecoder, EntityEncoder }
import org.http4s.circe.{ jsonEncoderOf, jsonOf }

case class CreateLinkRequest(url: String)

object CreateLinkRequest {
  implicit val decoderRequest: EntityDecoder[IO, CreateLinkRequest] =
    jsonOf[IO, CreateLinkRequest]

  implicit val encoderRequest: EntityEncoder[IO, CreateLinkRequest] =
    jsonEncoderOf[IO, CreateLinkRequest]
}
case class CreateLinkResponse(url: String)

object CreateLinkResponse {
  implicit val decoderResponse: EntityDecoder[IO, CreateLinkResponse] =
    jsonOf[IO, CreateLinkResponse]

  implicit val encoderResponse: EntityEncoder[IO, CreateLinkResponse] =
    jsonEncoderOf[IO, CreateLinkResponse]

}

case class Link(url: String, shortUrl: String)

object Link {
  implicit val decoderResponse: EntityDecoder[IO, Link] =
    jsonOf[IO, Link]

  implicit val encoderResponse: EntityEncoder[IO, Link] =
    jsonEncoderOf[IO, Link]

}

case class ListLinksResponse(links: List[Link])

object ListLinksResponse {
  implicit val decoderResponse: EntityDecoder[IO, ListLinksResponse] =
    jsonOf[IO, ListLinksResponse]

  implicit val encoderResponse: EntityEncoder[IO, ListLinksResponse] =
    jsonEncoderOf[IO, ListLinksResponse]

}

def buildAndRunApp = {
  for {
    storage <- RedisLinkStorage.build()
    server <- new LinkShortenerHttpServer(storage).build()
  } yield server

}

object MyApp extends IOApp {
  def run(args: List[String]): IO[ExitCode] = buildAndRunApp.use(_ => IO.never).as(ExitCode.Success)
}
