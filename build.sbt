name := "Archivum"

version := "0.3.0"

scalaVersion := "2.12.4"

scalacOptions += "-Ypartial-unification"
libraryDependencies += "org.typelevel" %% "cats-core" % "1.0.1"

libraryDependencies += "org.scalafx" %% "scalafx" % "8.0.144-R12"
libraryDependencies += "io.spray" %%  "spray-json" % "1.3.3"
// https://mvnrepository.com/artifact/org.scala-lang.modules/scala-xml
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.6"

// Scala 2.10, 2.11, 2.12
libraryDependencies ++= Seq(
   "org.scalikejdbc" %% "scalikejdbc"       % "3.2.1",
   "com.h2database"  %  "h2"                 % "1.4.196",
   "ch.qos.logback"  %  "logback-classic"   % "1.2.3"
)

// https://mvnrepository.com/artifact/org.controlsfx/controlsfx
libraryDependencies += "org.controlsfx" % "controlsfx" % "8.40.14"

libraryDependencies += "org.typelevel" %% "cats-core" % "1.0.1"

libraryDependencies += "org.parboiled" %% "parboiled" % "2.1.5"

// TODO assembly setup
mainClass in assembly := Some("com.radeusgd.archivum.gui.ApplicationMain")

libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.12.4"