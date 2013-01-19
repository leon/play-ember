import sbt._
import sbt.Keys._

object ProjectBuild extends Build {

  lazy val buildVersion =  "0.1-SNAPSHOT"

  lazy val root = Project(id = "play-ember", base = file("."), settings = Project.defaultSettings ++ Publish.settings ++ Ls.settings).settings(
    sbtPlugin := true,
    organization := "se.radley",
    description := "PlayFramework 2 Ember Handlebars Precompile Plugin",
    version := buildVersion,
    scalaVersion := "2.10.0",
    parallelExecution in Test := false,
    testFrameworks += TestFrameworks.Specs2,

    resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
    resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",

    libraryDependencies ++= Seq(
      "play" %% "play" % "2.1-RC2" % "provided",
      "play" %% "play-test" % "2.1-RC2" % "test",
      "org.mozilla" % "rhino" % "1.7R4",
      "commons-io" % "commons-io" % "2.4"
    ),

    libraryDependencies <++= (scalaVersion, sbtVersion)((scalaVersion, sbtVersion) =>
      Seq("play" % "sbt-plugin" % "2.1-RC2" % "provided->default(compile)" extra ("scalaVersion" -> "2.9.2", "sbtVersion" -> "0.12"))
    )
  )
}

object Publish {
  lazy val settings = Seq(
    publishMavenStyle := true,
    publishTo <<= version { (v: String) =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    homepage := Some(url("https://github.com/leon/play-ember")),
    pomExtra := (
      <scm>
        <url>git://github.com/leon/play-ember.git</url>
        <connection>scm:git://github.com/leon/play-ember.git</connection>
      </scm>
      <developers>
        <developer>
          <id>leon</id>
          <name>Leon Radley</name>
          <url>http://github.com/leon</url>
        </developer>
      </developers>)
  )
}

object Ls {

  import _root_.ls.Plugin.LsKeys._

  lazy val settings = _root_.ls.Plugin.lsSettings ++ Seq(
    (description in lsync) := "Play Framework 2 Ember Handlebars Precompile Plugin",
    licenses in lsync <<= licenses,
    (tags in lsync) := Seq("play", "playframework", "ember", "handlebars", "precompile"),
    (docsUrl in lsync) := Some(new URL("https://github.com/leon/play-ember"))
  )
}