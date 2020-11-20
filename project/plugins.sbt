val localPath =
  java.nio.file.Path.of("../sbt-legal-review-helper.jar").toUri.toASCIIString

addSbtPlugin("com.typesafe.sbt" % "sbt-license-report" % "1.2.0")
addSbtPlugin("org.enso" % "sbt-license-report" % "0.1.0" from localPath)
