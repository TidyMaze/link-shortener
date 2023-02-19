package fr.yaro.link

import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import cats.effect.IO
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}

case class CreateLinkRequest(url: String)

object CreateLinkRequest {
  implicit val decoderRequest: EntityDecoder[IO, CreateLinkRequest] =
    jsonOf[IO, CreateLinkRequest]

  implicit val encoderRequest: EntityEncoder[IO, CreateLinkRequest] =
    jsonEncoderOf[IO, CreateLinkRequest]
}
