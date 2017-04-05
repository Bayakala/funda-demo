name := "funda-demo"

version := "1.8"

scalaVersion := "2.11.8"

resolvers +=  Resolver.bintrayRepo("bayakala","maven")

libraryDependencies += "com.bayakala" %% "funda" % "1.0.0-RC-01" withSources() withJavadoc()
