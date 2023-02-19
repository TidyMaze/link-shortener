package fr.yaro.link

import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import cats.effect.IO
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}

case class ListLinksResponse(links: List[Link])

object ListLinksResponse {
  implicit val decoderResponse: EntityDecoder[IO, ListLinksResponse] =
    jsonOf[IO, ListLinksResponse]

  implicit val encoderResponse: EntityEncoder[IO, ListLinksResponse] =
    jsonEncoderOf[IO, ListLinksResponse]
}
