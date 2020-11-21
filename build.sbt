import sbt._
import org.enso.licensehelperplugin.{Distribution, GatherLicenses}

version := "1.2.4"

scalaVersion := "2.12.12"

GatherLicenses.licenseConfigurations := Set("compile")
GatherLicenses.configurationRoot := file("tools/legal-review")
GatherLicenses.reportIssueExplanation := "Verify the reports."
GatherLicenses.dependencyFilters := Seq()

lazy val gatherLicenses =
  taskKey[Unit]("Gathers licensing information for relevant dependencies")
gatherLicenses := {
  GatherLicenses.run.value
}
lazy val verifyLicensePackages =
  taskKey[Unit](
    "Verifies if the license package has been generated, " +
      "has no warnings and is up-to-date with dependencies."
  )
verifyLicensePackages := GatherLicenses.verifyReports.value
GatherLicenses.distributions := Seq(
  Distribution(
    "archivum",
    file("THIRD-PARTY"),
    Distribution.sbtProjects(root)
  ),
)

lazy val openLegalReviewReport =
  taskKey[Unit](
    "Gathers licensing information for relevant dependencies and opens the " +
      "report in review mode in the browser."
  )
openLegalReviewReport := {
  val _ = gatherLicenses.value
  GatherLicenses.runReportServer()
}

lazy val analyzeDependency = inputKey[Unit]("...")
analyzeDependency := GatherLicenses.analyzeDependency.evaluated

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "archivum",
    organization := "com.radeusgd"
  )
  .settings(
    libraryDependencies ++= Seq(
      "com.norbitltd" %% "spoiwo" % "1.4.1",
      "org.typelevel" %% "cats-core" % "1.0.1",
      "org.scalafx" %% "scalafx" % "8.0.144-R12",
      "io.spray" %% "spray-json" % "1.3.3",
      // https://mvnrepository.com/artifact/org.scala-lang.modules/scala-xml
      "org.scala-lang.modules" %% "scala-xml" % "1.3.0",

      "org.scalikejdbc" %% "scalikejdbc" % "3.2.1",
      "com.h2database" % "h2" % "1.4.196",
      "org.slf4j" % "slf4j-simple" % "1.7.30",
      "org.controlsfx" % "controlsfx" % "8.40.14",
      "org.parboiled" %% "parboiled" % "2.1.5",
      "org.scala-lang" % "scala-compiler" % scalaVersion.value // this will be needed for runtime compilation,
    )
  )
  .settings(
    scalacOptions += "-Ypartial-unification",
    // TODO assembly setup
    mainClass in assembly := Some("com.radeusgd.archivum.gui.ApplicationMain"),
      assemblyExcludedJars in assembly := {
      val cp = (fullClasspath in assembly).value
      val excluded = cp filter {_.data.getName == "h2-1.4.196.jar"}
      val log = streams.value.log
        log.info(s"Excluded JARs are $excluded")
        excluded
    }
    // TODO auto-generate launcher scripts
  )
