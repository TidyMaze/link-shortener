package fr.yaro.link

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import com.redis.RedisClient
import org.http4s.dsl.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.implicits.uri
import org.http4s.client.dsl.io.*
import org.http4s.headers.*
import org.http4s.{Header, Headers, MediaType, Response, Uri}
import org.http4s.dsl.io.{GET, PUT}
import org.typelevel.ci.CIString
import org.http4s.client.Client
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.ExecutionContext.global

class MyAppTest
    extends AsyncWordSpec
    with AsyncIOSpec
    with Matchers
    with BeforeAndAfterAll {
  val httpClient = EmberClientBuilder.default[IO].build

  def purgeRedis(): Unit = {
    val redisClient = new RedisClient("localhost", 6379)
    redisClient.flushall
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    // delete everything in redis
    purgeRedis()
  }

  "a call to create an URL" should {
    "create and return a short url" in {

      val longUri = uri"""https://www.google.com"""

      buildAndRunApp.use(server => {
        val serverUri = server.baseUri
        IO(println("Server started on address " + serverUri.toString)) *> {
          for {
            res <- httpClient.use(callCreateUrl(serverUri, _, longUri))
          } yield assert(res.toString.startsWith("http://localhost:8080/"))
        }
      })
    }
  }

  "a call to access a shortened url" should {
    "return the full url as a redirect" in {

      val longUri = uri"""https://www.abc.com"""

      buildAndRunApp.use(server => {
        val serverUri = server.baseUri
        IO(println("Server started on address " + serverUri.toString)) *> {
          for {
            shortened <- httpClient.use(callCreateUrl(serverUri, _, longUri))
            accessResponse <- httpClient.use(getExpandedUri(shortened, _))
          } yield assert(accessResponse.contains(longUri))
        }
      })
    }

  }

  private def getExpandedUri(
      shortened: Uri,
      httpClient: Client[IO]
  ): IO[Option[Uri]] = {
    httpClient.get(shortened)(response =>
      IO(
        response.headers
          .get(CIString("Location"))
          .map(l => Uri.unsafeFromString(l.head.value))
      )
    )
  }

  private def callCreateUrl(
      serverAddress: Uri,
      httpClient: Client[IO],
      longUrl: Uri
  ): IO[Uri] = {
    val fullUri = serverAddress / "create"
    httpClient
      .expect[CreateLinkResponse](
        PUT(
          CreateLinkRequest(longUrl.toString),
          fullUri,
          Headers("Content-Type" -> "application/json")
        )
      )
      .map(_.url)
      .map(Uri.unsafeFromString)
  }
}
