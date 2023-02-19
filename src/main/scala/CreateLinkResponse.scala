package fr.yaro.link

import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import cats.effect.IO
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}

case class CreateLinkResponse(url: String)

object CreateLinkResponse {
  implicit val decoderResponse: EntityDecoder[IO, CreateLinkResponse] =
    jsonOf[IO, CreateLinkResponse]

  implicit val encoderResponse: EntityEncoder[IO, CreateLinkResponse] =
    jsonEncoderOf[IO, CreateLinkResponse]

}
