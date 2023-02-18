package fr.yaro.link

import cats.effect.IO
import cats.effect.kernel.Resource
import com.redis.RedisClient
import cats.implicits._

trait LinkStorage {
  def createLink(url: String): IO[String]
  def expandLink(shortUrl: String): IO[Option[String]]

  def listLinks(): IO[List[Link]]
}

class RedisLinkStorage(redisClient: RedisClient, shortener: Shortener)
    extends LinkStorage {

  override def createLink(url: String): IO[String] =
    IO(redisClient.get(url)).flatMap {
      case Some(shortUrl) => IO.pure(shortUrl)
      case None =>
        val shortUrl = shortener.shorten(url)
        for {
          // to find the original url from the short url
          forward <- IO(redisClient.set(shortUrlToLongUrlKey(shortUrl), url))
          // to find the short url from the original url
          backward <- IO(redisClient.set(longUrlToShortUrlKey(url), shortUrl))
        } yield shortUrl
    }

  override def expandLink(shortUrl: String): IO[Option[String]] =
    IO(redisClient.get(shortUrlToLongUrlKey(shortUrl)))

  private def shortUrlToLongUrlKey(shortUrl: String): String =
    s"rev-link-$shortUrl"

  private def longUrlToShortUrlKey(longUrl: String): String =
    s"link-$longUrl"

  override def listLinks(): IO[List[Link]] =
    IO(redisClient.keys(longUrlToShortUrlKey("*")))
      .map(_.getOrElse(List.empty[Option[String]]))
      .map(_.flatten)
      .flatMap { keys =>
        keys.map { key =>
          for {
            shortUrl <- IO(redisClient.get(key)).flatMap(
              _.map(IO.pure).getOrElse(
                IO.raiseError(new RuntimeException(s"Could not find $key"))
              )
            )
            url <- IO(redisClient.get(shortUrlToLongUrlKey(shortUrl))).flatMap(
              _.map(IO.pure).getOrElse(
                IO.raiseError(
                  new RuntimeException(s"Could not find rev-link-$shortUrl")
                )
              )
            )
          } yield Link(url, shortUrl)
        }.sequence
      }
}

object RedisLinkStorage {
  def build(): Resource[IO, LinkStorage] = {
    val port = 6379
    val host = "localhost"
    val acquire = IO(new RedisClient(host, port)).handleErrorWith { e =>
      IO.raiseError(
        new RuntimeException(s"Could not connect to Redis at $host:$port", e)
      )
    }
    val release: RedisClient => IO[Unit] = redisClient =>
      IO(redisClient.close())
    Resource
      .make(acquire)(release)
      .map(new RedisLinkStorage(_, new Shortener()))
  }
}
