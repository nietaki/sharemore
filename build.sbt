name := """sharemore_2_3"""

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.3.0"
  //"org.webjars" % "bootstrap" % "2.3.1",
  //"org.webjars" % "requirejs" % "2.1.11-1"
)

scalaVersion := "2.11.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala)
