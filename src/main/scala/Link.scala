package fr.yaro.link

import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import cats.effect.IO
import org.http4s.{ EntityDecoder, EntityEncoder }
import org.http4s.circe.{ jsonEncoderOf, jsonOf }

case class Link(url: String, shortUrl: String, useCount: Int)

object Link {
  implicit val decoderResponse: EntityDecoder[IO, Link] =
    jsonOf[IO, Link]

  implicit val encoderResponse: EntityEncoder[IO, Link] =
    jsonEncoderOf[IO, Link]

}
