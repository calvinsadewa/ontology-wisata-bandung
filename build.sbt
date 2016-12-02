name := """ontology-wisata-bandung"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "net.sourceforge.owlapi" % "owlapi-distribution" % "5.0.4",
  "net.sourceforge.owlapi" % "org.semanticweb.hermit" % "1.3.8.500"
)



fork in run := true