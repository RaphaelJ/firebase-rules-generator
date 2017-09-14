import Dependencies._

name            := "firebase-rules-generator"
organization    := "com.bloomlife"
scalaVersion    := "2.12.1"
version         := "0.1.1"

// Allows the use of higher-kinded types.
scalacOptions += "-feature"

libraryDependencies ++= Seq(
  "com.typesafe.play" % "play-json_2.12" % "2.6.0-M7",
  "org.scalaz" % "scalaz-core_2.12" % "7.2.11"
)
