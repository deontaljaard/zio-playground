import sbt._

object Dependencies {

  case object com {
    case object github {
      val `pureconfig-core` =
        "com.github.pureconfig" %% "pureconfig" % "0.17.0"
    }
  }

  case object dev {
    case object zio {
      val zio =
        dependency("zio")

      val `zio-test` =
        dependency("zio-test")

      val `zio-test-sbt` =
        dependency("zio-test-sbt")

      val `zio-interop-cats` =
        "dev.zio" %% "zio-interop-cats" % "3.1.1.0"

      private def dependency(artifact: String): ModuleID =
        "dev.zio" %% artifact % "1.0.12"
    }
  }

  case object io {
    case object circe {
      val `circe-core` =
        dependency("circe-core")

      val `circe-generic` =
        dependency("circe-generic")

      val `circe-parser` =
        dependency("circe-parser")

      private def dependency(artifact: String): ModuleID =
        "io.circe" %% artifact % "0.14.1"
    }
  }

  case object org {

    case object flyway {
      val `flyway-core` =
        "org.flywaydb" % "flyway-core" % "8.0.2"
    }

    case object http4s {
      val `http4s-blaze-server` =
        dependency("blaze-server")

      val `http4s-circe` =
        dependency("circe")

      val `http4s-dsl` =
        dependency("dsl")

      private def dependency(artifact: String): ModuleID =
        "org.http4s" %% s"http4s-$artifact" % "1.0.0-M23"
    }

    case object tpolecat {
      val `doobie-core` =
        dependency("doobie-core")

      val `doobie-hikari` =
        dependency("doobie-hikari")

      val `doobie-postgres` =
        dependency("doobie-postgres")

      val `doobie-specs2` =
        dependency("doobie-specs2")

      def dependency(artifactId: String): ModuleID =
        "org.tpolecat" %% artifactId % "1.0.0-M3"
    }

    case object typelevel {
      val `kind-projector` =
        "org.typelevel" % "kind-projector" % "0.13.2"
    }

    case object scalatest {
      val scalatest =
        "org.scalatest" %% "scalatest" % "3.2.10"
    }

    case object scalatestplus {
      val `scalacheck-1-15` =
        "org.scalatestplus" %% "scalacheck-1-15" % "3.2.10.0"
    }
  }
}
