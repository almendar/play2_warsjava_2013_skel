import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "warsjawa2013"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    "org.scalatest" % "scalatest_2.10" % "2.0.M8" % "test",
    "org.webjars" %% "webjars-play" % "2.1.0-3",
    "org.webjars" % "bootstrap" % "3.0.0",
    "org.webjars" % "jquery-ui" % "1.9.0"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
