import sbt.*

object Dependencies {
  lazy val sparkVersion = "3.5.0"
  lazy val catsEffect: Seq[ModuleID] = Seq(
    "org.typelevel" %% "kittens" % "3.0.0",
    "org.typelevel" %% "cats-effect" % "3.5.2",
    "org.typelevel" %% "cats-mtl" % "1.3.0"
  )

  lazy val dataProcess: Seq[ModuleID] = Seq(
    "io.scalaland" %% "chimney" % "0.8.0",
    "io.scalaland" %% "chimney-cats" % "0.8.0",
    "org.typelevel" %% "cats-core" % "2.10.0",
    "org.typelevel" %% "kittens" % "3.0.0"
  )

  lazy val tagless: Seq[ModuleID] = Seq(
    "org.typelevel" %% "cats-tagless-macros" % "0.15.0"
  )

  lazy val configFramework: Seq[ModuleID] = Seq(
    "com.github.pureconfig" %% "pureconfig" % "0.17.4",
    "com.github.pureconfig" %% "pureconfig-cats-effect" % "0.17.4" excludeAll(
      ExclusionRule(organization = "org.typelevel")),
    "com.github.pureconfig" %% "pureconfig-generic" % "0.17.4"
  )

  lazy val spark: Seq[ModuleID] = Seq(
    "org.apache.spark" %% "spark-core" % "3.5.0",
    "org.apache.spark" %% "spark-sql" % "3.5.0",
    "org.apache.spark" %% "spark-streaming" % "3.5.0"
  )//.map(_ % Provided)

  lazy val scalaTest: Seq[ModuleID] = Seq(
    "org.scalactic" %% "scalactic" % "3.2.17",
    "org.scalatest" %% "scalatest" % "3.2.17" % Test
  )
}
