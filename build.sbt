import Dependencies.*

name := "spark_fp"

version := "0.1"


ThisBuild / organization := "com.example"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.12"

//addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)


lazy val commonSettings = Seq(
  addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full),
  addCompilerPlugin ("com.olegpy" %% "better-monadic-for" % "0.3.1"),
  scalacOptions ++= Seq(
    "-language:implicitConversions",
    "-language:higherKinds",
    "-language:existentials",
    "-language:postfixOps",
    "-Ymacro-annotations"
  )
)

lazy val framework = (project in file("framework"))
  .settings(
    commonSettings
  )
  .settings (
    libraryDependencies ++= (catsEffect ++ spark ++ tagless ++ configFramework ++ scalaTest)
  )

lazy val core = (project in file("core"))
  .settings(
    commonSettings
  )

lazy val demo = (project in file("demo"))
  .settings(
    commonSettings
  )
  .settings(
    libraryDependencies ++= configFramework ++ spark ++ scalaTest
  )
  .dependsOn(framework, core)

lazy val root = (project in file("."))
  .dependsOn(demo)
  .aggregate(framework, core, demo)

assembly / assemblyJarName := "demo-1.0.jar"