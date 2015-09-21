name := """place_star"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  cache,
  ws,
  specs2 % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

routesGenerator := InjectedRoutesGenerator

libraryDependencies += "com.sksamuel.scrimage" %% "scrimage-core" % "2.1.0"
libraryDependencies += "commons-codec" % "commons-codec" % "1.10"
