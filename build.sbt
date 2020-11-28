import sbt._
import org.enso.licensehelperplugin.{Distribution, GatherLicenses}
import sbt.nio.file.FileTreeView

import scala.collection.parallel.CollectionsHaveToParArray

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

lazy val distribute = taskKey[Unit]("Prepare package for distribution")

Global / onChangedBuildSource := ReloadOnSourceChanges

val h2dbVersion = "1.4.196"

def shouldBeSeparateDependency(file: Attributed[File]): Boolean =
  file.data.getName == s"h2-$h2dbVersion.jar"

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
      "com.h2database" % "h2" % h2dbVersion,
      "org.slf4j" % "slf4j-simple" % "1.7.30",
      "org.controlsfx" % "controlsfx" % "8.40.14",
      "org.parboiled" %% "parboiled" % "2.1.5",
      "org.scala-lang" % "scala-compiler" % scalaVersion.value // this will be needed for runtime compilation,
    )
  )
  .settings(
    scalacOptions += "-Ypartial-unification",
    // TODO assembly setup
    assembly / mainClass := Some("com.radeusgd.archivum.gui.ApplicationMain"),
    assembly / assemblyExcludedJars  := {
      val cp = (assembly / fullClasspath).value
      val excluded = cp filter shouldBeSeparateDependency
      if (excluded.isEmpty) {
        throw new IllegalStateException("No files were excluded but we wanted some, did the algorithm get out of date?")
      }
      val log = streams.value.log
        log.info(s"Excluded JARs are $excluded")
        excluded
    }
  )
  .settings(
    distribute :=  Def.task {
      val log = streams.value.log
      val versionString = version.value
      val mainJAR = (assembly / assemblyOutputPath).value
      val classPath = (assembly / fullClasspath).value
      val excluded = classPath filter shouldBeSeparateDependency
      val mainClassName = (assembly / mainClass).value.getOrElse(throw new IllegalStateException("Main class not set"))

      if (excluded.length != 1) {
        throw new IllegalStateException("currently we expect exactly 1 excluded dependency")
      }
      val h2JAR = excluded.head.data
      val mainJARfilename = mainJAR.toPath.getFileName.toString
      val h2JARfilename = h2JAR.toPath.getFileName.toString
      val packageName = s"Archivum-$versionString.zip"
      log.info(s"Creating package $packageName")

      val windowsCode =
        s"""cd ARCHIVUMBASE=%~dp0
           |java -cp $h2JARfilename;$mainJARfilename $mainClassName""".stripMargin
      val unixCode =
        s"""#!/usr/bin/env bash
           |cd $$(dirname "$$0")
           |java -cp $h2JARfilename:$mainJARfilename $mainClassName""".stripMargin
      val windowsScript = file("archivum_windows.bat")
      val unixScript = file("archivum_macos_linux.sh")

      IO.write(windowsScript, windowsCode)
      IO.write(unixScript, unixCode)
      val permissions ="rwxrwxr-x"
      IO.chmod(permissions, windowsScript)
      IO.chmod(permissions, unixScript)

      val directories = Seq(
        file("Konfiguracja"),
        file("THIRD-PARTY")
      )
      val files = Seq(
        file("README.md"),
        file("LICENSE"),
        mainJAR,
        h2JAR,
        windowsScript,
        unixScript
      )

      val zipFile = file(packageName)
      val zipPath = zipFile.toPath.toAbsolutePath.normalize.toString
      IO.delete(zipFile)
      IO.withTemporaryDirectory { tmp =>
        for (dir <- directories) IO.copyDirectory(dir, tmp / dir.name)
        for (f <- files) IO.copyFile(f, tmp / f.name)
        val exitCode = sys.process.Process(Seq("zip", "-r", zipPath, "."), cwd = tmp).!
        if (exitCode != 0) {
          throw new RuntimeException("Zip failed")
        }
      }

      log.info(s"Successfully packaged $packageName")
    }.dependsOn(assembly).value
  )
