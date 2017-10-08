name := "funda-demo"

version := "1.8"

scalaVersion := "2.11.8"

resolvers +=  Resolver.bintrayRepo("bayakala","maven")
//resolvers += Resolver.mavenLocal

libraryDependencies ++= Seq(
  "com.bayakala" %% "funda" % "1.0.0-RC-03" withSources() withJavadoc(),
  "com.typesafe.akka" %% "akka-actor" % "2.5.4",
  "com.typesafe.akka" %% "akka-stream" % "2.5.4"
)
