import sbt.Keys._
import sbt._

val currentScalaVersion = "2.11.7"

scalaVersion := currentScalaVersion

val commonSettings = Seq(
  scalaVersion := currentScalaVersion,
  resolvers += Resolver.url("Moorka", url("http://dl.bintray.com/tenderowls/moorka"))(Resolver.ivyStylePatterns),
  version := "0.7.1",
  organization := "com.tenderowls.opensource",
  libraryDependencies ++= Seq(
    "org.reactivekittens" %%% "moorka" % "0.7.1",
    "org.reactivekittens" %%% "felix" % "0.7.1"
  ),
  licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")),
  homepage := Some(url("http://github.com/tenderowls/moorka-exapmles")),
  scalacOptions ++= Seq("-deprecation", "-feature")
)

lazy val `moorka-todomvc` = (project in file("moorka-todomvc"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(
    // Move compiled JS files to a js sub-directory
    Seq(packageJSDependencies, fastOptJS, fullOptJS) map { packageJSKey ⇒
      Keys.crossTarget in(Compile, packageJSKey) := (classDirectory in Compile).value / "js"
    }: _*
  )

lazy val root = (project in file("."))
  .settings(
    scalaVersion := currentScalaVersion
  )
  .aggregate(
    `moorka-todomvc`
  )
