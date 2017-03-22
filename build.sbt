name := "funda-demo"

version := "1.7"

scalaVersion := "2.11.8"

resolvers += Resolver.mavenLocal

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.2.0",
  "com.typesafe.slick" %% "slick-testkit" % "3.2.0" % "test",
  "org.slf4j" % "slf4j-nop" % "1.7.21",
  "com.h2database" % "h2" % "1.4.191",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.0",
  "com.bayakala" % "funda_2.11" % "1.0.0-SNAPSHOT" withSources() withJavadoc()
)
