package fr.yaro.link

import cats.effect.*
import com.comcast.ip4s.{ ipv4, port }
import org.http4s.dsl.io.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.{ EntityDecoder, EntityEncoder }
import org.http4s.circe.{ jsonEncoderOf, jsonOf }

def buildAndRunApp = {
  for {
    storage <- RedisLinkStorage.build()
    server <- new LinkShortenerHttpServer(storage).build()
  } yield server

}

object MyApp extends IOApp {
  def run(args: List[String]): IO[ExitCode] = buildAndRunApp.use(_ => IO.never).as(ExitCode.Success)
}
