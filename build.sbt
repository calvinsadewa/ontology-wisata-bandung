name := """ontology-wisata-bandung"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "org.apache.jena" % "jena-core" % "3.1.1",
  "org.apache.jena" % "jena-arq" % "3.1.1"
)



fork in run := true