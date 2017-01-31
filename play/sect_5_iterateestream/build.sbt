import play.PlayScala

name := "iteratee-stream"

version := "1.0"

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  ws,
  "org.scalatestplus" %% "play" % "1.1.0-RC1" % "test"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)
