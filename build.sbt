import sbt._
import sbt.Keys._

val currentScalaVersion = "2.11.4"

scalaVersion := currentScalaVersion

val commonSettings = Seq(
  scalaVersion := currentScalaVersion,
  resolvers += Resolver.url("Moorka", url("http://dl.bintray.com/tenderowls/moorka"))(Resolver.ivyStylePatterns),
  version := "0.2.0",
  organization := "com.tenderowls.opensource",
  libraryDependencies ++= Seq(
    "com.tenderowls.opensource" %%%! "moorka-ui" % "0.2.0"
  ),
  licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")),
  homepage := Some(url("http://github.com/tenderowls/moorka-exapmles")),
  scalacOptions ++= Seq("-deprecation", "-feature")
)

lazy val `moorka-todomvc` = (project in file("moorka-todomvc"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings:_*)

lazy val root = (project in file("."))
  .settings(
    scalaVersion := currentScalaVersion
  )
  .aggregate(
    `moorka-todomvc`
  )

