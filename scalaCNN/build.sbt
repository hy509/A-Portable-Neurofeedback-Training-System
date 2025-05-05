/*scalaVersion := "2.12.13"

scalacOptions ++= Seq(
  "-feature",
  "-language:reflectiveCalls",
)

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases")
)

// Chisel 3.5
addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % "3.5.0" cross CrossVersion.full)
libraryDependencies += "edu.berkeley.cs" %% "chisel3" % "3.5.0"
libraryDependencies += "edu.berkeley.cs" %% "chiseltest" % "0.5.0"*/
scalaVersion := "2.12.17"

scalacOptions := Seq("-deprecation", "-Xsource:2.11")

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)

// Chisel 3.4
libraryDependencies += "edu.berkeley.cs" %% "chisel3" % "3.4.3"
libraryDependencies += "edu.berkeley.cs" %% "chisel-iotesters" % "1.5.3"
libraryDependencies += "edu.berkeley.cs" %% "chiseltest" % "0.3.3"
javaOptions += "-Xmx8G"