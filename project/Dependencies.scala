import sbt._

object Dependencies {
  lazy val catsEffect    = "org.typelevel"     %% "cats-effect"     % "2.2.0"
  lazy val refined       = "eu.timepit"        %% "refined"         % "0.9.17"
  lazy val circe         = "io.circe"          %% "circe-parser"    % "0.13.0"
  lazy val scalatest     = "org.scalatestplus" %% "scalacheck-1-14" % "3.1.2.0" % Test
  lazy val kindProjector = "org.typelevel"     %% "kind-projector"  % "0.10.3" cross CrossVersion.binary
}
