package playground.zioplayground

import zio._

package object config {
  type AppConfig = Has[AppConfig.Config]
  type HttpConfig = Has[HttpConfig.Config]

  val getAppConfig: URIO[AppConfig, AppConfig.Config] =
    ZIO.access(_.get)

  val getHttpConfig: URIO[HttpConfig, HttpConfig.Config] =
    ZIO.access(_.get)
}
