package playground
package zioplayground

import cats.data.Kleisli
import cats.effect._
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware.CORS
import org.http4s.{HttpApp, Request, Response}
import playground.zioplayground.config.{HttpConfig, getAppConfig}
import playground.zioplayground.country.http.CountryService
import playground.zioplayground.health.http.HealthService
import zio.interop.catz._
import zio.{ExitCode => ZExitCode, _}

object Main extends App {
  type AppTask[A] = RIO[layers.AppEnv, A]

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ZExitCode] =
    app
      .provideSomeLayer[ZEnv](layers.live.appLayer)
      .orDie

  val app: ZIO[layers.AppEnv, Throwable, ZExitCode] =
    for {
      cfg <- getAppConfig
      _ <- zio.console.putStrLn(s"Starting with config: $cfg")
      app = httpApp()
      _ <- runHttp(app, cfg.http)
    } yield ZExitCode.success

  def httpApp(): Kleisli[AppTask, Request[AppTask], Response[AppTask]] =
    Router[AppTask](
      "/health-check" -> HealthService.routes(),
      "/countries" -> CountryService.routes()
    ).orNotFound

  def runHttp[R <: layers.BaseModules](
                                         httpApp: HttpApp[RIO[R, *]], // * is kind-projector notation
                                         httpConfig: HttpConfig.Config
                                       ): ZIO[R, Throwable, Unit] = {
    type Task[A] = RIO[R, A]
    ZIO.runtime[R].flatMap { implicit rts =>
      BlazeServerBuilder
        .apply[Task](rts.platform.executor.asEC)
        .bindHttp(httpConfig.port, httpConfig.host)
        .withHttpApp(CORS(httpApp))
        .serve
        .compile[Task, Task, ExitCode]
        .drain
    }
  }
}
