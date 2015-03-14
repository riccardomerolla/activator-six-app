import play.PlayScala

name := "activator-gilt-app"

scalaVersion := "2.11.6"

lazy val root = (project in file(".")).enablePlugins(SbtWeb)

lazy val core = (project in file("core")).enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .settings(commonPlaySettings: _*)
  .settings(
    version := "1.0-SNAPSHOT",
    // Temporary addition until api.json is moved.
    unmanagedClasspath in Test += (baseDirectory in ThisBuild).value / "svc"
  )

lazy val svc = (project in file("svc")).enablePlugins(PlayScala)
  .dependsOn(core)
  .aggregate(core)
  .settings(commonSettings: _*)
  .settings(commonPlaySettings: _*)
  .settings(
    version := "1.0-SNAPSHOT"
  )

lazy val web = (project in file("web")).enablePlugins(PlayScala)
  .dependsOn(core)
  .aggregate(core)
  .settings(commonSettings: _*)
  .settings(commonPlaySettings: _*)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      "org.webjars" %% "webjars-play" % "2.3.0-2",
      "org.webjars" % "bootstrap" % "3.1.1-2"
    )
  )

lazy val commonPlaySettings: Seq[Setting[_]] = Seq(
  libraryDependencies ++= Seq(
    ws,
    jdbc,
    anorm,
    "postgresql" % "postgresql" % "9.1-901.jdbc4"
  )
)

lazy val commonSettings: Seq[Setting[_]] = Seq(
  name <<= name("activator-gilt-app-" + _),
  libraryDependencies ++= Seq(
    ws,
    "org.scalatest" %% "scalatest" % "2.1.5" % "test"
  )
)


fork in run := true