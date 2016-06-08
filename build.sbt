name := """play"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "com.novus" %% "salat" % "latest.release",
  "org.slf4j" % "slf4j-nop" % "latest.release",
  "org.mongodb" %% "casbah" % "latest.release",
  "org.webjars" % "jquery" % "latest.release"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

includeFilter in (Assets, LessKeys.less) := "style.less"
