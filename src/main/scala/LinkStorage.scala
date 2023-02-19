package fr.yaro.link

import cats.effect.IO
import cats.effect.kernel.Resource
import com.redis.RedisClient
import cats.implicits._
import com.redis.serialization.Parse.Implicits.parseInt

trait LinkStorage {
  def createLink(url: String): IO[String]
  def expandLink(shortUrl: String): IO[Option[String]]

  def listLinks(): IO[List[Link]]
}

class RedisLinkStorage(redisClient: RedisClient, shortener: Shortener)
    extends LinkStorage {

  override def createLink(url: String): IO[String] =
    IO(redisClient.get[String](url)).flatMap {
      case Some(shortUrl) => IO.pure(shortUrl)
      case None =>
        val shortUrl = shortener.shorten(url)
        for {
          // to find the original url from the short url
          forward <- IO(redisClient.set(shortUrlToLongUrlKey(shortUrl), url))
          // to find the short url from the original url
          backward <- IO(redisClient.set(longUrlToShortUrlKey(url), shortUrl))
          // to count the number of times the short url has been used
          count <- IO(redisClient.set(s"count-$shortUrl", 0))
        } yield shortUrl
    }

  override def expandLink(shortUrl: String): IO[Option[String]] =
    IO(redisClient.get(shortUrlToLongUrlKey(shortUrl)))

  private def shortUrlToLongUrlKey(shortUrl: String): String =
    s"rev-link-$shortUrl"

  private def longUrlToShortUrlKey(longUrl: String): String =
    s"link-$longUrl"

  override def listLinks(): IO[List[Link]] =
    listAllLongToShortKeys
      .flatMap { keys =>
        keys.map { key =>
          for {
            shortUrl <- IO(redisClient.get[String](key)).flatMap(
              _.map(IO.pure).getOrElse(
                IO.raiseError(new RuntimeException(s"Could not find $key"))
              )
            )
            url <- IO(redisClient.get[String](shortUrlToLongUrlKey(shortUrl)))
              .flatMap(
                _.map(IO.pure).getOrElse(
                  IO.raiseError(
                    new RuntimeException(s"Could not find rev-link-$shortUrl")
                  )
                )
              )

            count <- IO(redisClient.get[Int](s"count-$shortUrl")).flatMap(
              _.map(IO.pure).getOrElse(
                IO.raiseError(
                  new RuntimeException(s"Could not find count-$shortUrl")
                )
              )
            )
          } yield Link(url, shortUrl, count)
        }.sequence
      }

  private def listAllLongToShortKeys: IO[List[String]] =
    IO(redisClient.keys[String](longUrlToShortUrlKey("*")))
      .map(_.toList.flatten.flatten)
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
