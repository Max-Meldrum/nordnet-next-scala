name := "nordnet-next-scala"

version := "1.0"

val http4sVersion = "0.17.5"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "org.scalatest" %% "scalatest" % "3.0.4" % "test",
  // Optional for auto-derivation of JSON codecs
  "io.circe" %% "circe-generic" % "0.8.0",
  // Optional for string interpolation to JSON model
  "io.circe" %% "circe-literal" % "0.8.0"
)
    