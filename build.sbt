import sbt._
import Keys._
import xerial.sbt.Sonatype._

inThisBuild(
  Seq(
    organization := "com.github.mvv.sredded",
    version := "0.1-M2", // next is M3
    homepage := Some(url("https://github.com/mvv/sredded")),
    scmInfo := Some(ScmInfo(url("https://github.com/mvv/sredded"), "scm:git@github.com:mvv/sredded.git")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(id = "mvv",
                name = "Mikhail Vorozhtsov",
                email = "mikhail.vorozhtsov@gmail.com",
                url = url("https://github.com/mvv"))
    ),
    sonatypeProjectHosting := Some(GitHubHosting("mvv", "sredded", "mikhail.vorozhtsov@gmail.com"))
  )
)

ThisBuild / publishTo := sonatypePublishToBundle.value
ThisBuild / publishMavenStyle := true

lazy val sonatypeBundleReleaseIfNotSnapshot: Command = Command.command("sonatypeBundleReleaseIfNotSnapshot") { state =>
  val extracted = Project.extract(state)
  if (extracted.get(isSnapshot)) {
    val log = extracted.get(sLog)
    log.info("Snapshot version, doing nothing")
    state
  } else {
    Command.process("sonatypeBundleRelease", state)
  }
}

inThisBuild(
  Seq(
    crossScalaVersions := Seq("2.13.2", "2.12.11"),
    scalaVersion := crossScalaVersions.value.head,
    scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked", "-Xfatal-warnings")
  )
)

def isPriorTo2_13(version: String): Boolean =
  CrossVersion.partialVersion(version) match {
    case Some((2, minor)) => minor < 13
    case _                => false
  }

lazy val specs2 = "org.specs2" %% "specs2-core" % "4.9.4"

lazy val sredded = (project in file("."))
  .settings(
    skip in publish := true,
    sonatypeProfileName := "com.github.mvv",
    sonatypeSessionName := s"Sredded_${version.value}",
    commands += sonatypeBundleReleaseIfNotSnapshot
  )
  .aggregate(core, generic, json)

lazy val core = (project in file("core"))
  .settings(
    name := "sredded",
    description := "Structured data representations",
    libraryDependencies += specs2 % Test
  )

lazy val generic = (project in file("generic"))
  .settings(
    name := "sredded-generic",
    description := "Structured data representations derivation macro for case classes",
    scalacOptions ++= {
      if (isPriorTo2_13(scalaVersion.value)) {
        Nil
      } else {
        Seq("-Ymacro-annotations")
      }
    },
    libraryDependencies ++=
      Seq(
        "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided,
        specs2 % Test
      ),
    libraryDependencies ++= {
      if (isPriorTo2_13(scalaVersion.value)) {
        Seq(compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full))
      } else {
        Nil
      }
    }
  )
  .dependsOn(core)

lazy val json = (project in file("json"))
  .settings(
    name := "sredded-json",
    description := "No-deps JSON printer for structured data",
    libraryDependencies += specs2 % Test
  )
  .dependsOn(core)
