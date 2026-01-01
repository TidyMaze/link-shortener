ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.1"

val http4sVersion = "0.23.18"

lazy val root = (project in file("."))
  .settings(
    name := "link-shortener",
    idePackagePrefix := Some("fr.yaro.link"),
    Keys.libraryDependencies ++= Seq(
      // get up and running with a client and server
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      // for core classes and traits, e.g. `Client[F]`
      "org.http4s" %% "http4s-core" % http4sVersion,
      "org.http4s" %% "http4s-client" % http4sVersion,
      "org.http4s" %% "http4s-server" % http4sVersion,
      // for circe JSON support
      "org.http4s" %% "http4s-circe" % http4sVersion,
      // Optional for auto-derivation of JSON codecs
      "io.circe" %% "circe-generic" % "0.14.3",
      // Optional for string interpolation to JSON model
      "io.circe" %% "circe-literal" % "0.14.3",
      // redis client for scala 2.13
      ("net.debasishg" %% "redisclient" % "3.42")
        .cross(CrossVersion.for3Use2_13),
      "ch.qos.logback" % "logback-classic" % "1.1.3",
      "org.typelevel" %% "cats-effect-testing-scalatest" % "1.5.0" % Test,
      "org.scalactic" %% "scalactic" % "3.2.15",
      "org.scalatest" %% "scalatest" % "3.2.15" % "test"
    )
  )
