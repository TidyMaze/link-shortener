package fr.yaro.link

import java.util.UUID

class Shortener {
  def shorten(url: String): String = UUID.randomUUID().toString
}
