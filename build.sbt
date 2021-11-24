import Dependencies.{io => _io, _}

addCompilerPlugin(org.typelevel.`kind-projector` cross CrossVersion.full)
enablePlugins(JavaAppPackaging, DockerPlugin)

lazy val `zio-playground` =
  project
    .in(file("."))
    .settings(thisBuildSettings)
    .settings(commonSettings)
    .settings(dependencies)
    .settings(dockerSettings)

lazy val thisBuildSettings = Seq(
  name := "zio-playground",
  organization := "playground",
  scalaVersion := "2.13.6",
)

lazy val commonSettings = commonScalacOptions ++ Seq(
  update / evictionWarningOptions := EvictionWarningOptions.empty,
  testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
)

lazy val commonScalacOptions = Seq(
  Compile / console / scalacOptions --= Seq(
    "-Wunused:_",
    "-deprecation",
    "-feature",
    "-language:implicitConversions",
    "-unchecked",
    "-Xfatal-warnings",
  ),
  Test / console / scalacOptions :=
    (Compile / console / scalacOptions).value,
)

lazy val dependencies = Seq(
  // main dependencies
  libraryDependencies ++= Seq(
    com.github.`pureconfig-core`,
    dev.zio.zio,
    dev.zio.`zio-interop-cats`,
    _io.circe.`circe-core`,
    _io.circe.`circe-generic`,
    _io.circe.`circe-parser`,
    org.flyway.`flyway-core`,
    org.http4s.`http4s-blaze-server`,
    org.http4s.`http4s-dsl`,
    org.http4s.`http4s-circe`,
    org.tpolecat.`doobie-core`,
    org.tpolecat.`doobie-hikari`,
    org.tpolecat.`doobie-postgres`,
  ),
  // test dependencies
  libraryDependencies ++= Seq(
    dev.zio.`zio-test`,
    dev.zio.`zio-test-sbt`,
    org.scalatest.scalatest,
    org.scalatestplus.`scalacheck-1-15`,
    org.tpolecat.`doobie-specs2`,
  ).map(_ % Test),
)

lazy val dockerSettings = Seq(
  dockerBaseImage := "openjdk:11-jre-slim-buster",
  Docker / dockerExposedPorts := Seq(8080),
  Docker / dockerUsername := Some("deontaljaard"),
  Docker / dockerRepository := Some("hub.docker.com")
)
