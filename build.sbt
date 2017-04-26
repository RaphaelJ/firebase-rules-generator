import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.1",
      version      := "0.1.0"
    )),
    name := "Firebase Rules Generator",
    libraryDependencies += "com.typesafe.play" % "play-json_2.12" % "2.6.0-M7"
  )
