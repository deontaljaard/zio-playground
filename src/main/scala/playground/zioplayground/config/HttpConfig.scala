package playground.zioplayground.config

import pureconfig.ConfigConvert
import pureconfig.generic.semiauto.deriveConvert
import zio._

object HttpConfig {
  final case class Config(port: Int,
                          baseUrl: String,
                          host: String)

  object Config {
    implicit val convert: ConfigConvert[Config] = deriveConvert
  }

  val fromAppConfig: ZLayer[AppConfig, Nothing, HttpConfig] =
    ZLayer.fromService(_.http)

}
