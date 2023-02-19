package fr.yaro.link

import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import cats.effect.IO
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}

case class GetFullUrlResponse(link: Link)

object GetFullUrlResponse {
  implicit val decoder: EntityDecoder[IO, GetFullUrlResponse] =
    jsonOf[IO, GetFullUrlResponse]
  implicit val encoder: EntityEncoder[IO, GetFullUrlResponse] =
    jsonEncoderOf[IO, GetFullUrlResponse]
}
